package com.smartcampus.erp.controller;

import com.smartcampus.erp.dto.EnrollmentRequest;
import com.smartcampus.erp.dto.EnrollmentResponse;
import com.smartcampus.erp.security.UserPrincipal;
import com.smartcampus.erp.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Enrollment", description = "Course enrollment and drop APIs for students, with registrar lookups for faculty/admin")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Enroll in a course", description = "Register the authenticated student in a course by its ID. Changes status to ENROLLED.")
    @ApiResponse(responseCode = "200", description = "Successfully enrolled in the course")
    @ApiResponse(responseCode = "400", description = "Course not found or student already enrolled")
    public ResponseEntity<EnrollmentResponse> enroll(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.enroll(userPrincipal.getId(), request.getCourseId()));
    }

    @PostMapping("/drop")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Drop a course", description = "Remove the student from a course. Changes enrollment status to DROPPED.")
    @ApiResponse(responseCode = "200", description = "Successfully dropped the course")
    @ApiResponse(responseCode = "400", description = "Not currently enrolled in this course")
    public ResponseEntity<EnrollmentResponse> drop(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.drop(userPrincipal.getId(), request.getCourseId()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get student's own enrollments", description = "Retrieve course enrollment history and statuses for the logged-in student.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved enrollment history")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(enrollmentService.getStudentEnrollments(userPrincipal.getId()));
    }

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    @Operation(summary = "Get course roster/enrollments", description = "Retrieve list of all student enrollments for a specific course ID. Accessible by Faculty and Admins.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved course roster")
    public ResponseEntity<List<EnrollmentResponse>> getCourseEnrollments(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getCourseEnrollments(courseId));
    }
}
