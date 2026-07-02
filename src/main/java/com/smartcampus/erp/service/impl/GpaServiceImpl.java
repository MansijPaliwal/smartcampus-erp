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
    @Override
    @Transactional(readOnly = true)
    public GpaResponse calculateGpa(Long studentUserId) {
        if (!studentProfileRepository.existsById(studentUserId)) {
            throw new ResourceNotFoundException("Student profile not found for user ID: " + studentUserId);
        }

        List<Marks> allMarks = marksRepository.findByStudentId(studentUserId);
        if (allMarks.isEmpty()) {
            return GpaResponse.builder()
                    .studentId(studentUserId)
                    .gpa(0.0)
                    .courseGrades(new ArrayList<>())
                    .build();
        }

        // Group by course ID
        Map<Course, List<Marks>> marksByCourse = allMarks.stream()
                .collect(Collectors.groupingBy(Marks::getCourse));

        List<CourseGradeDto> courseGrades = new ArrayList<>();
        java.math.BigDecimal totalWeightedGradePoints = java.math.BigDecimal.ZERO;
        int totalCredits = 0;

        for (Map.Entry<Course, List<Marks>> entry : marksByCourse.entrySet()) {
            Course course = entry.getKey();
            List<Marks> courseMarks = entry.getValue();

            java.math.BigDecimal totalObtained = courseMarks.stream()
                    .map(Marks::getMarksObtained)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            java.math.BigDecimal totalMax = courseMarks.stream()
                    .map(Marks::getMaxMarks)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            java.math.BigDecimal percentage = java.math.BigDecimal.ZERO;
            if (totalMax.compareTo(java.math.BigDecimal.ZERO) > 0) {
                percentage = totalObtained.divide(totalMax, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(java.math.BigDecimal.valueOf(100));
            }

            String letterGrade;
            int gradePoint;

            double pctDouble = percentage.doubleValue();
            if (pctDouble >= 90.0) {
                letterGrade = "O";
                gradePoint = 10;
            } else if (pctDouble >= 80.0) {
                letterGrade = "A+";
                gradePoint = 9;
            } else if (pctDouble >= 70.0) {
                letterGrade = "A";
                gradePoint = 8;
            } else if (pctDouble >= 60.0) {
                letterGrade = "B+";
                gradePoint = 7;
            } else if (pctDouble >= 50.0) {
                letterGrade = "B";
                gradePoint = 6;
            } else if (pctDouble >= 40.0) {
                letterGrade = "C";
                gradePoint = 5;
            } else {
                letterGrade = "F";
                gradePoint = 0;
            }

            int credits = course.getCredits();
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

        double finalGpa = 0.0;
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
