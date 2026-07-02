package com.smartcampus.erp.controller;

import com.smartcampus.erp.dto.*;
import com.smartcampus.erp.security.UserPrincipal;
import com.smartcampus.erp.service.GpaService;
import com.smartcampus.erp.service.StudentService;
import com.smartcampus.erp.service.AiAdvisorService;
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
@RequestMapping("/api/students")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student", description = "Student profile, course registration lookup, attendance, marks, and GPA calculator APIs")
public class StudentController {

    private final StudentService studentService;
    private final GpaService gpaService;
    private final AiAdvisorService aiAdvisorService;

    public StudentController(StudentService studentService, GpaService gpaService, AiAdvisorService aiAdvisorService) {
        this.studentService = studentService;
        this.gpaService = gpaService;
        this.aiAdvisorService = aiAdvisorService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get student profile", description = "Retrieve profile details of the currently logged-in student.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved profile details")
    @ApiResponse(responseCode = "404", description = "Student profile not initialized")
    public ResponseEntity<StudentProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(studentService.getProfile(userPrincipal.getId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Initialize or update profile", description = "Create a new student profile or edit existing details (roll number, department, semester, dob, phone).")
    @ApiResponse(responseCode = "200", description = "Successfully updated student profile")
    @ApiResponse(responseCode = "400", description = "Invalid request arguments")
    public ResponseEntity<StudentProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StudentProfileRequest request) {
        return ResponseEntity.ok(studentService.createOrUpdateProfile(userPrincipal.getId(), request));
    }

    @GetMapping("/courses")
    @Operation(summary = "Get enrolled courses", description = "Retrieve list of all courses the student is currently enrolled in.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved enrolled courses")
    public ResponseEntity<List<CourseResponse>> getEnrolledCourses(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(studentService.getEnrolledCourses(userPrincipal.getId()));
    }

    @GetMapping("/attendance")
    @Operation(summary = "Get attendance records", description = "Retrieve attendance logs. Optionally filter by course ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved attendance records")
    public ResponseEntity<List<AttendanceResponse>> getAttendance(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(studentService.getAttendance(userPrincipal.getId(), courseId));
    }

    @GetMapping("/marks")
    @Operation(summary = "Get exam grades/marks", description = "Retrieve list of marks scored by the student in all exams.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved grades")
    public ResponseEntity<List<MarksResponse>> getMarks(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(studentService.getMarks(userPrincipal.getId()));
    }

    @GetMapping("/gpa")
    @Operation(summary = "Calculate dynamic GPA", description = "Calculate dynamic GPA and retrieve grade breakdown based on marks scored.")
    @ApiResponse(responseCode = "200", description = "Successfully calculated GPA")
    public ResponseEntity<GpaResponse> getGpa(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(gpaService.calculateGpa(userPrincipal.getId()));
    }

    @GetMapping("/fees")
    @Operation(summary = "Get fee payments status", description = "Retrieve total dues, paid payments, pending bills, and transactions history.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved fee status")
    public ResponseEntity<StudentFeeStatusResponse> getFeeStatus(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(studentService.getFeeStatus(userPrincipal.getId()));
    }

    @GetMapping("/gpa/insights")
    @Operation(summary = "Get AI-generated academic insights", description = "Feeds dynamic course scores into GPT-4o-mini to generate an optimization blueprint.")
    public ResponseEntity<java.util.Map<String, String>> getAiInsights(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        String insight = aiAdvisorService.generateGpaStrategyInsight(userPrincipal.getId());
        return ResponseEntity.ok(java.util.Map.of("aiInsight", insight));
    }
}
