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
        double totalWeightedGradePoints = 0.0;
        int totalCredits = 0;

        for (Map.Entry<Course, List<Marks>> entry : marksByCourse.entrySet()) {
            Course course = entry.getKey();
            List<Marks> courseMarks = entry.getValue();

            double totalObtained = courseMarks.stream().mapToDouble(Marks::getMarksObtained).sum();
            double totalMax = courseMarks.stream().mapToDouble(Marks::getMaxMarks).sum();

            double percentage = totalMax > 0 ? (totalObtained / totalMax) * 100 : 0.0;

            String letterGrade;
            int gradePoint;

            if (percentage >= 90.0) {
                letterGrade = "O";
                gradePoint = 10;
            } else if (percentage >= 80.0) {
                letterGrade = "A+";
                gradePoint = 9;
            } else if (percentage >= 70.0) {
                letterGrade = "A";
                gradePoint = 8;
            } else if (percentage >= 60.0) {
                letterGrade = "B+";
                gradePoint = 7;
            } else if (percentage >= 50.0) {
                letterGrade = "B";
                gradePoint = 6;
            } else if (percentage >= 40.0) {
                letterGrade = "C";
                gradePoint = 5;
            } else {
                letterGrade = "F";
                gradePoint = 0;
            }

            int credits = course.getCredits();
            totalWeightedGradePoints += (gradePoint * credits);
            totalCredits += credits;

            courseGrades.add(CourseGradeDto.builder()
                    .courseCode(course.getCode())
                    .courseTitle(course.getTitle())
                    .percentage(Math.round(percentage * 100.0) / 100.0) // round to 2 decimals
                    .letterGrade(letterGrade)
                    .gradePoint(gradePoint)
                    .credits(credits)
                    .build());
        }

        double finalGpa = totalCredits > 0 ? totalWeightedGradePoints / totalCredits : 0.0;
        // Round GPA to 2 decimal places
        finalGpa = Math.round(finalGpa * 100.0) / 100.0;

        return GpaResponse.builder()
                .studentId(studentUserId)
                .gpa(finalGpa)
                .courseGrades(courseGrades)
                .build();
    }
}
