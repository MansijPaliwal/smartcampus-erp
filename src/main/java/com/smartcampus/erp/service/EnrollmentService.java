package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {
    EnrollmentResponse enroll(Long studentUserId, Long courseId);
    EnrollmentResponse drop(Long studentUserId, Long courseId);
    List<EnrollmentResponse> getStudentEnrollments(Long studentUserId);
    List<EnrollmentResponse> getCourseEnrollments(Long courseId);
}
