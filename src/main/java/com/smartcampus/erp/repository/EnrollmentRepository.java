package com.smartcampus.erp.repository;

import com.smartcampus.erp.entity.Enrollment;
import com.smartcampus.erp.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<Enrollment> findByCourseId(Long courseId);
    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);
}
