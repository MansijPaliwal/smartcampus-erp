package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.AttendanceRecordRequest;
import com.smartcampus.erp.dto.FacultyProfileRequest;
import com.smartcampus.erp.dto.FacultyProfileResponse;
import com.smartcampus.erp.dto.MarksRequest;

public interface FacultyService {
    FacultyProfileResponse getProfile(Long facultyUserId);
    FacultyProfileResponse createOrUpdateProfile(Long facultyUserId, FacultyProfileRequest request);
    void markAttendance(Long facultyUserId, AttendanceRecordRequest request);
    void enterMarks(Long facultyUserId, MarksRequest request);
}
