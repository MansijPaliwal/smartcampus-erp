package com.smartcampus.erp.service;

import java.util.Map;

public interface ExamAnalyticsService {
    /**
     * Aggregates a student's academic profile (grades & attendance patterns) and computes
     * predictive success scores and warning flags for academic oversight dashboards.
     *
     * @param studentUserId The ID of the student.
     * @return Map containing predictive metrics, course breakdowns, and grade estimates.
     */
    Map<String, Object> calculateStudentPerformanceTrends(Long studentUserId);
}
