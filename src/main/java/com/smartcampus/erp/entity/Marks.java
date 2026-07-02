package com.smartcampus.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "marks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "course_id", "exam_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "exam_type", nullable = false)
    private String examType; // MIDTERM, FINAL, QUIZ, etc.

    @Column(name = "marks_obtained", nullable = false)
    private Double marksObtained;

    @Column(name = "max_marks", nullable = false)
    private Double maxMarks;
}
