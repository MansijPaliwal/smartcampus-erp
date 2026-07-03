package com.smartcampus.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_forms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamForm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private String examId;

    @Column(name = "candidate_name", nullable = false)
    private String candidateName;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus; // PENDING or PAID

    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;

    @Column(name = "allocated_exam_date")
    private String allocatedExamDate;

    @Column(name = "exam_center")
    private String examCenter;

    @Column(name = "subject_details", length = 2000)
    private String subjectDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_profile_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private StudentProfile studentProfile;
}
