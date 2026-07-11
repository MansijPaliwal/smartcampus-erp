package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.dto.CreateFeeDueRequest;
import com.smartcampus.erp.dto.FeePaymentRequest;
import com.smartcampus.erp.dto.FeePaymentResponse;
import com.smartcampus.erp.entity.FeePayment;
import com.smartcampus.erp.entity.PaymentStatus;
import com.smartcampus.erp.entity.StudentProfile;
import com.smartcampus.erp.entity.TransactionLedger;
import com.smartcampus.erp.exception.BadRequestException;
import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.exception.UnauthorizedException;
import com.smartcampus.erp.repository.FeePaymentRepository;
import com.smartcampus.erp.repository.StudentProfileRepository;
import com.smartcampus.erp.repository.TransactionLedgerRepository;
import com.smartcampus.erp.service.FeePaymentService;
import com.smartcampus.erp.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FeePaymentServiceImpl implements FeePaymentService {

    private final FeePaymentRepository feePaymentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TransactionLedgerRepository transactionLedgerRepository;
    private final NotificationService notificationService;

    public FeePaymentServiceImpl(FeePaymentRepository feePaymentRepository,
                                 StudentProfileRepository studentProfileRepository,
                                 TransactionLedgerRepository transactionLedgerRepository,
                                 NotificationService notificationService) {
        this.feePaymentRepository = feePaymentRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.transactionLedgerRepository = transactionLedgerRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public FeePaymentResponse createFeeDue(CreateFeeDueRequest request) {
        StudentProfile student = studentProfileRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for ID: " + request.getStudentId()));

        FeePayment feePayment = FeePayment.builder()
                .student(student)
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .build();

        FeePayment saved = feePaymentRepository.save(feePayment);

        // Record entry in the TransactionLedger: DEBIT tuition fee
        writeLedgerEntry(student, request.getAmount(), BigDecimal.ZERO, "Tuition Fee Due Created");

        // Notify Student
        String msg = String.format("A new tuition fee due of %.2f has been registered on your account.", request.getAmount().doubleValue());
        notificationService.createNotification(student.getId(), "Fee Dues Created", msg);

        return mapToFeePaymentResponse(saved);
    }

    @Override
    @Transactional
    public FeePaymentResponse payFee(Long studentUserId, Long feePaymentId, FeePaymentRequest request) {
        FeePayment feePayment = feePaymentRepository.findById(feePaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee payment record not found for ID: " + feePaymentId));

        if (!feePayment.getStudent().getId().equals(studentUserId)) {
            throw new UnauthorizedException("Student is not authorized to pay this fee record");
        }

        if (feePayment.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("This fee has already been paid");
        }

        String txId = request.getTransactionId();
        if (txId == null || txId.trim().isEmpty()) {
            txId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        // Verify if transaction ID is unique
        if (feePaymentRepository.findByTransactionId(txId).isPresent()) {
            throw new BadRequestException("Transaction ID already exists: " + txId);
        }

        feePayment.setStatus(PaymentStatus.PAID);
        feePayment.setPaymentDate(LocalDateTime.now());
        feePayment.setTransactionId(txId);
        feePayment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CARD");

        // Note: we can verify request.getAmount() matches feePayment.getAmount() if needed
        if (request.getAmount().compareTo(feePayment.getAmount()) != 0) {
             throw new BadRequestException(String.format("Payment amount (%.2f) does not match fee amount (%.2f)",
                     request.getAmount().doubleValue(), feePayment.getAmount().doubleValue()));
        }

        FeePayment saved = feePaymentRepository.save(feePayment);

        // Record entry in the TransactionLedger: CREDIT payment
        writeLedgerEntry(feePayment.getStudent(), BigDecimal.ZERO, feePayment.getAmount(), "Tuition Fee Paid - " + txId);

        // Notify Student
        String msg = String.format("Your payment of %.2f has been successfully recorded under Transaction ID: %s.",
                saved.getAmount().doubleValue(), saved.getTransactionId());
        notificationService.createNotification(studentUserId, "Fee Payment Successful", msg);

        return mapToFeePaymentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeePaymentResponse> getAllPayments() {
        return feePaymentRepository.findAll().stream()
                .map(this::mapToFeePaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void processPaymentWebhook(Map<String, Object> payload) {
        if (!payload.containsKey("paymentId")) {
            throw new BadRequestException("Webhook payload missing paymentId field");
        }

        Long paymentId;
        Object rawPaymentId = payload.get("paymentId");
        if (rawPaymentId instanceof Number) {
            paymentId = ((Number) rawPaymentId).longValue();
        } else {
            paymentId = Long.valueOf(rawPaymentId.toString());
        }

        String transactionId = (String) payload.getOrDefault("transactionId", "TXN-WEB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        String statusStr = (String) payload.getOrDefault("status", "SUCCESS");

        FeePayment feePayment = feePaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee payment record not found for ID: " + paymentId));

        if (feePayment.getStatus() == PaymentStatus.PAID) {
            // Deduplicate: already paid
            return;
        }

        if ("SUCCESS".equalsIgnoreCase(statusStr)) {
            feePayment.setStatus(PaymentStatus.PAID);
            feePayment.setPaymentDate(LocalDateTime.now());
            feePayment.setTransactionId(transactionId);
            feePayment.setPaymentMethod("WEBHOOK");
            feePaymentRepository.save(feePayment);

            // Record entry in the TransactionLedger: CREDIT payment
            writeLedgerEntry(feePayment.getStudent(), BigDecimal.ZERO, feePayment.getAmount(), 
                    "Tuition Fee Paid (Webhook) - Transaction ID: " + transactionId);

            // Notify Student
            String msg = String.format("Your payment of %.2f has been successfully recorded via Webhook under Transaction ID: %s.",
                    feePayment.getAmount().doubleValue(), transactionId);
            notificationService.createNotification(feePayment.getStudent().getId(), "Payment Successful (Webhook)", msg);

        } else if ("FAILED".equalsIgnoreCase(statusStr)) {
            feePayment.setStatus(PaymentStatus.FAILED);
            feePaymentRepository.save(feePayment);

            // Notify Student of failure
            String msg = String.format("Your payment attempt of %.2f has failed.", feePayment.getAmount().doubleValue());
            notificationService.createNotification(feePayment.getStudent().getId(), "Payment Failed (Webhook)", msg);
        }
    }

    private void writeLedgerEntry(StudentProfile student, BigDecimal debit, BigDecimal credit, String description) {
        var latestOpt = transactionLedgerRepository.findFirstByStudentIdOrderByIdDesc(student.getId());
        var previousBalance = BigDecimal.ZERO;
        var previousHash = "0000000000000000000000000000000000000000000000000000000000000000";

        if (latestOpt.isPresent()) {
            previousBalance = latestOpt.get().getBalance();
            previousHash = latestOpt.get().getCurrentHash();
        }

        // Double-entry running balance rule: balance = previousBalance + debit - credit
        var balance = previousBalance.add(debit).subtract(credit);

        var entry = TransactionLedger.builder()
                .student(student)
                .debit(debit)
                .credit(credit)
                .balance(balance)
                .description(description)
                .createdAt(LocalDateTime.now())
                .previousHash(previousHash)
                .build();

        transactionLedgerRepository.save(entry);
    }

    private FeePaymentResponse mapToFeePaymentResponse(FeePayment payment) {
        return FeePaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus().name())
                .transactionId(payment.getTransactionId())
                .studentId(payment.getStudent().getId())
                .studentName(payment.getStudent().getUser().getName())
                .paymentMethod(payment.getPaymentMethod())
                .build();
    }
}
