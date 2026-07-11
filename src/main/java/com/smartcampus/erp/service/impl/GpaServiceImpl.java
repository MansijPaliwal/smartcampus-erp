package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.dto.CourseGradeDto;
import com.smartcampus.erp.dto.GpaResponse;
import com.smartcampus.erp.entity.Course;
import com.smartcampus.erp.entity.Marks;
import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.repository.MarksRepository;
import com.smartcampus.erp.repository.StudentProfileRepository;
import com.smartcampus.erp.service.GpaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GpaServiceImpl implements GpaService {

    private final MarksRepository marksRepository;
    private final StudentProfileRepository studentProfileRepository;

    public GpaServiceImpl(MarksRepository marksRepository, StudentProfileRepository studentProfileRepository) {
        this.marksRepository = marksRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    /**
     * Calculates the dynamically computed GPA of a student.
     *
     * GRADING SCALE & FORMULA DEFINITION:
     * 1. Gather all marks scored by the student in the database.
     * 2. Group the marks by Course.
     * 3. For each Course, aggregate the marks obtained and the max marks:
     *    Percentage = (Sum of Marks Obtained / Sum of Maximum Marks) * 100
     * 4. Map the Percentage to a standard Letter Grade and Grade Point:
     *    - Percentage >= 90.0% : Grade 'O' (Outstanding)     -> Grade Point = 10
     *    - Percentage >= 80.0% : Grade 'A+' (Excellent)      -> Grade Point = 9
     *    - Percentage >= 70.0% : Grade 'A' (Very Good)       -> Grade Point = 8
     *    - Percentage >= 60.0% : Grade 'B+' (Good)            -> Grade Point = 7
     *    - Percentage >= 50.0% : Grade 'B' (Above Average)   -> Grade Point = 6
     *    - Percentage >= 40.0% : Grade 'C' (Average)         -> Grade Point = 5
     *    - Percentage < 40.0%  : Grade 'F' (Fail)            -> Grade Point = 0
     * 5. GPA is the credit-weighted average of these grade points:
     *    GPA = Sum(Grade Point * Course Credits) / Sum(Course Credits)
     *    If the sum of course credits is 0, GPA defaults to 0.0.
     */
    private record GradeInfo(String letterGrade, int gradePoint) {}

    private GradeInfo determineGrade(double pct) {
        if (pct >= 90.0) {
            return new GradeInfo("O", 10);
        } else if (pct >= 80.0) {
            return new GradeInfo("A+", 9);
        } else if (pct >= 70.0) {
            return new GradeInfo("A", 8);
        } else if (pct >= 60.0) {
            return new GradeInfo("B+", 7);
        } else if (pct >= 50.0) {
            return new GradeInfo("B", 6);
        } else if (pct >= 40.0) {
            return new GradeInfo("C", 5);
        } else {
            return new GradeInfo("F", 0);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GpaResponse calculateGpa(Long studentUserId) {
        if (!studentProfileRepository.existsById(studentUserId)) {
            throw new ResourceNotFoundException("Student profile not found for user ID: " + studentUserId);
        }

        var allMarks = marksRepository.findByStudentId(studentUserId);
        if (allMarks.isEmpty()) {
            return GpaResponse.builder()
                    .studentId(studentUserId)
                    .gpa(0.0)
                    .courseGrades(new ArrayList<>())
                    .build();
        }

        // Group by course ID
        var marksByCourse = allMarks.stream()
                .collect(Collectors.groupingBy(Marks::getCourse));

        var courseGrades = new ArrayList<CourseGradeDto>();
        var totalWeightedGradePoints = java.math.BigDecimal.ZERO;
        var totalCredits = 0;

        for (var entry : marksByCourse.entrySet()) {
            var course = entry.getKey();
            var courseMarks = entry.getValue();

            var totalObtained = courseMarks.stream()
                    .map(Marks::getMarksObtained)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            var totalMax = courseMarks.stream()
                    .map(Marks::getMaxMarks)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            var percentage = java.math.BigDecimal.ZERO;
            if (totalMax.compareTo(java.math.BigDecimal.ZERO) > 0) {
                percentage = totalObtained.divide(totalMax, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(java.math.BigDecimal.valueOf(100));
            }

            var gradeInfo = determineGrade(percentage.doubleValue());
            var letterGrade = gradeInfo.letterGrade();
            var gradePoint = gradeInfo.gradePoint();

            var credits = course.getCredits();
            totalWeightedGradePoints = totalWeightedGradePoints.add(
                    java.math.BigDecimal.valueOf(gradePoint).multiply(java.math.BigDecimal.valueOf(credits))
            );
            totalCredits += credits;

            courseGrades.add(CourseGradeDto.builder()
                    .courseCode(course.getCode())
                    .courseTitle(course.getTitle())
                    .percentage(percentage.setScale(2, java.math.RoundingMode.HALF_UP).doubleValue())
                    .letterGrade(letterGrade)
                    .gradePoint(gradePoint)
                    .credits(credits)
                    .build());
        }

        var finalGpa = 0.0;
        if (totalCredits > 0) {
            finalGpa = totalWeightedGradePoints.divide(java.math.BigDecimal.valueOf(totalCredits), 2, java.math.RoundingMode.HALF_UP).doubleValue();
        }

        return GpaResponse.builder()
                .studentId(studentUserId)
                .gpa(finalGpa)
                .courseGrades(courseGrades)
                .build();
    }
}
