package com.smartcampus.erp.controller;

import com.smartcampus.erp.dto.*;
import com.smartcampus.erp.security.UserPrincipal;
import com.smartcampus.erp.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@Tag(name = "Assignment", description = "Course assignments creation APIs for faculty, student submissions (with file uploads), and grading/marks updates")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Create an assignment", description = "Publish a new assignment for a course. Only accessible by the assigned Course Faculty.")
    @ApiResponse(responseCode = "201", description = "Successfully created assignment")
    @ApiResponse(responseCode = "400", description = "Course not assigned to this faculty")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.createAssignment(userPrincipal.getId(), request));
    }

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    @Operation(summary = "Get course assignments", description = "Retrieve list of all assignments published for a specific course ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved assignments list")
    public ResponseEntity<List<AssignmentResponse>> getCourseAssignments(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getCourseAssignments(courseId));
    }

    @PostMapping(value = "/{assignmentId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit an assignment", description = "Upload a solution file (max 5MB, PDF/DOCX/ZIP) for an assignment.")
    @ApiResponse(responseCode = "200", description = "Successfully submitted assignment")
    @ApiResponse(responseCode = "400", description = "File size exceeds 5MB or invalid file type")
    public ResponseEntity<SubmissionResponse> submitAssignment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(assignmentService.submitAssignment(userPrincipal.getId(), assignmentId, file));
    }

    @PostMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Grade an assignment submission", description = "Set grades and feedback comments for a student's assignment submission.")
    @ApiResponse(responseCode = "200", description = "Successfully graded submission")
    @ApiResponse(responseCode = "400", description = "Submission not found or not authorized to grade this course")
    public ResponseEntity<SubmissionResponse> gradeSubmission(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeSubmissionRequest request) {
        return ResponseEntity.ok(assignmentService.gradeSubmission(userPrincipal.getId(), submissionId, request));
    }

    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasRole('FACULTY')")
    @Operation(summary = "Get submissions for assignment", description = "Retrieve a list of all student submissions for a specific assignment ID. Accessible by Faculty.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved submissions list")
    public ResponseEntity<List<SubmissionResponse>> getAssignmentSubmissions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(assignmentService.getAssignmentSubmissions(userPrincipal.getId(), assignmentId));
    }

    @GetMapping("/my-submissions")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get student's own submissions", description = "Retrieve list of all submissions made by the logged-in student.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved submissions history")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(assignmentService.getStudentSubmissions(userPrincipal.getId()));
    }
}
