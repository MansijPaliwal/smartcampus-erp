package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.dto.CreateFeeDueRequest;
import com.smartcampus.erp.dto.FeePaymentRequest;
import com.smartcampus.erp.dto.FeePaymentResponse;
import com.smartcampus.erp.entity.FeePayment;
import com.smartcampus.erp.entity.PaymentStatus;
import com.smartcampus.erp.entity.StudentProfile;
import com.smartcampus.erp.exception.BadRequestException;
import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.exception.UnauthorizedException;
import com.smartcampus.erp.repository.FeePaymentRepository;
import com.smartcampus.erp.repository.StudentProfileRepository;
import com.smartcampus.erp.service.FeePaymentService;
import com.smartcampus.erp.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FeePaymentServiceImpl implements FeePaymentService {

    private final FeePaymentRepository feePaymentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final NotificationService notificationService;

    public FeePaymentServiceImpl(FeePaymentRepository feePaymentRepository,
                                 StudentProfileRepository studentProfileRepository,
                                 NotificationService notificationService) {
        this.feePaymentRepository = feePaymentRepository;
        this.studentProfileRepository = studentProfileRepository;
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

        // Notify Student
        String msg = String.format("A new tuition fee due of %.2f has been registered on your account.", request.getAmount());
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
        if (!request.getAmount().equals(feePayment.getAmount())) {
             throw new BadRequestException(String.format("Payment amount (%.2f) does not match fee amount (%.2f)",
                     request.getAmount(), feePayment.getAmount()));
        }

        FeePayment saved = feePaymentRepository.save(feePayment);

        // Notify Student
        String msg = String.format("Your payment of %.2f has been successfully recorded under Transaction ID: %s.",
                saved.getAmount(), saved.getTransactionId());
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
