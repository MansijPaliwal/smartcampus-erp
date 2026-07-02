package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AssignmentService {
    AssignmentResponse createAssignment(Long facultyUserId, AssignmentRequest request);
    List<AssignmentResponse> getCourseAssignments(Long courseId);
    SubmissionResponse submitAssignment(Long studentUserId, Long assignmentId, MultipartFile file);
    SubmissionResponse gradeSubmission(Long facultyUserId, Long submissionId, GradeSubmissionRequest request);
    List<SubmissionResponse> getAssignmentSubmissions(Long facultyUserId, Long assignmentId);
    List<SubmissionResponse> getStudentSubmissions(Long studentUserId);
}
