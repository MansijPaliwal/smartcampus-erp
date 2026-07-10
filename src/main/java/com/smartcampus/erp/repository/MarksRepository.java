package com.smartcampus.erp.repository;

import com.smartcampus.erp.entity.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface MarksRepository extends JpaRepository<Marks, Long> {
    List<Marks> findByStudentId(Long studentId);
    List<Marks> findByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<Marks> findByStudentIdAndCourseIdAndExamType(Long studentId, Long courseId, String examType);

    @Query("SELECT m.course.id as courseId, m.course.title as courseTitle, " +
           "AVG(m.marksObtained / m.maxMarks * 100) as avgPercentage " +
           "FROM Marks m WHERE m.student.id = :studentId " +
           "GROUP BY m.course.id, m.course.title")
    List<Map<String, Object>> getAverageMarksPerCourse(@Param("studentId") Long studentId);

    @Query("SELECT AVG(m.marksObtained / m.maxMarks * 100) FROM Marks m WHERE m.student.id = :studentId")
    Double getOverallAverageMarksPercentage(@Param("studentId") Long studentId);
}
