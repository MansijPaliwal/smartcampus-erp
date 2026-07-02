package com.smartcampus.erp.repository;

import com.smartcampus.erp.entity.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarksRepository extends JpaRepository<Marks, Long> {
    List<Marks> findByStudentId(Long studentId);
    List<Marks> findByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<Marks> findByStudentIdAndCourseIdAndExamType(Long studentId, Long courseId, String examType);
}
