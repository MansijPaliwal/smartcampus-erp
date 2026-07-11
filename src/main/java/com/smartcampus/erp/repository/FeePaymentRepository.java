package com.smartcampus.erp.repository;

import com.smartcampus.erp.entity.FeePayment;
import com.smartcampus.erp.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {
    List<FeePayment> findByStudentId(Long studentId);
    List<FeePayment> findByStudentIdAndStatus(Long studentId, PaymentStatus status);
    Optional<FeePayment> findByTransactionId(String transactionId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(f.amount), 0) FROM FeePayment f WHERE f.status = :status")
    java.math.BigDecimal sumAmountByStatus(@org.springframework.data.repository.query.Param("status") PaymentStatus status);
}
