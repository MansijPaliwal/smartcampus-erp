package com.smartcampus.erp.repository;

import com.smartcampus.erp.entity.ExamForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ExamFormRepository extends JpaRepository<ExamForm, Long> {
    Optional<ExamForm> findByStudentProfileIdAndExamId(Long studentProfileId, String examId);
    List<ExamForm> findByStudentProfileId(Long studentProfileId);
    Optional<ExamForm> findFirstByStudentProfileIdAndPaymentStatusOrderByIdDesc(Long studentProfileId, String paymentStatus);
}
