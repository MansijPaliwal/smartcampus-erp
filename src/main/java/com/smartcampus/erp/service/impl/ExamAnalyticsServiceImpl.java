package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.repository.AttendanceRepository;
import com.smartcampus.erp.repository.MarksRepository;
import com.smartcampus.erp.repository.StudentProfileRepository;
import com.smartcampus.erp.service.ExamAnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExamAnalyticsServiceImpl implements ExamAnalyticsService {

    private final StudentProfileRepository studentProfileRepository;
    private final MarksRepository marksRepository;
    private final AttendanceRepository attendanceRepository;

    public ExamAnalyticsServiceImpl(StudentProfileRepository studentProfileRepository,
                                    MarksRepository marksRepository,
                                    AttendanceRepository attendanceRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.marksRepository = marksRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> calculateStudentPerformanceTrends(Long studentUserId) {
        if (!studentProfileRepository.existsById(studentUserId)) {
            throw new ResourceNotFoundException("Student profile not found for user ID: " + studentUserId);
        }

        // 1. Gather historical attendance aggregates
        Long totalDays = attendanceRepository.countTotalAttendanceDays(studentUserId);
        Long presentDays = attendanceRepository.countPresentDays(studentUserId);
        
        totalDays = totalDays != null ? totalDays : 0L;
        presentDays = presentDays != null ? presentDays : 0L;
        
        double attendanceRate = totalDays > 0 ? (double) presentDays / totalDays : 1.0;

        // 2. Gather historical exam marks percentages
        Double averageMarks = marksRepository.getOverallAverageMarksPercentage(studentUserId);
        double overallAverageMarksPercentage = averageMarks != null ? averageMarks : 0.0;

        // 3. Compute course-specific averages
        List<Map<String, Object>> courseWiseAverages = marksRepository.getAverageMarksPerCourse(studentUserId);

        // 4. Run simple linear regression proxy to calculate predictive academic success index (0 to 100)
        // Weighted Formula: 80% weight on Marks performance, 20% weight on Attendance consistency
        double predictiveScore = (overallAverageMarksPercentage * 0.8) + (attendanceRate * 100.0 * 0.2);

        // 5. Predict CGPA estimation (standard 10.0 scale)
        double predictedCgpa = (predictiveScore / 100.0) * 10.0;
        predictedCgpa = Math.round(predictedCgpa * 100.0) / 100.0;

        // 6. Assess risk warning classification
        String riskCategory;
        if (attendanceRate < 0.75 || overallAverageMarksPercentage < 50.0) {
            riskCategory = "HIGH_RISK";
        } else if (attendanceRate < 0.85 || overallAverageMarksPercentage < 65.0) {
            riskCategory = "MEDIUM_RISK";
        } else {
            riskCategory = "LOW_RISK";
        }

        // 7. Structure the aggregate analytics dashboard payload
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("studentId", studentUserId);
        analytics.put("totalAttendanceSessions", totalDays);
        analytics.put("presentSessions", presentDays);
        analytics.put("attendancePercentage", Math.round(attendanceRate * 100.0 * 100.0) / 100.0);
        analytics.put("overallMarksPercentage", Math.round(overallAverageMarksPercentage * 100.0) / 100.0);
        analytics.put("predictiveSuccessIndex", Math.round(predictiveScore * 100.0) / 100.0);
        analytics.put("predictedCgpa", Math.min(10.0, Math.max(0.0, predictedCgpa)));
        analytics.put("riskCategory", riskCategory);
        analytics.put("coursePerformanceBreakdown", courseWiseAverages);

        return analytics;
    }
}
