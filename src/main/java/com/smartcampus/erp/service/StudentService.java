package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.*;

import java.util.List;

public interface StudentService {
    StudentProfileResponse getProfile(Long studentUserId);
    StudentProfileResponse createOrUpdateProfile(Long studentUserId, StudentProfileRequest request);
    List<CourseResponse> getEnrolledCourses(Long studentUserId);
    List<AttendanceResponse> getAttendance(Long studentUserId, Long courseId);
    List<MarksResponse> getMarks(Long studentUserId);
    StudentFeeStatusResponse getFeeStatus(Long studentUserId);
}
