package com.smartcampus.erp.repository;

import com.smartcampus.erp.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);
    List<AssignmentSubmission> findByStudentId(Long studentId);
}
