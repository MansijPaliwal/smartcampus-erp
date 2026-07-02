package com.smartcampus.erp.controller;

import com.smartcampus.erp.dto.AttendanceRecordRequest;
import com.smartcampus.erp.dto.FacultyProfileRequest;
import com.smartcampus.erp.dto.FacultyProfileResponse;
import com.smartcampus.erp.dto.MarksRequest;
import com.smartcampus.erp.security.UserPrincipal;
import com.smartcampus.erp.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faculties")
@PreAuthorize("hasRole('FACULTY')")
@Tag(name = "Faculty", description = "Faculty profile, class attendance grading, and markbook/grades entry APIs")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get faculty profile", description = "Retrieve department and designation details of the currently logged-in faculty member.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved profile details")
    @ApiResponse(responseCode = "404", description = "Faculty profile not initialized")
    public ResponseEntity<FacultyProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(facultyService.getProfile(userPrincipal.getId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Initialize or update profile", description = "Create a new faculty profile or edit existing details (department, designation).")
    @ApiResponse(responseCode = "200", description = "Successfully updated faculty profile")
    @ApiResponse(responseCode = "400", description = "Invalid request arguments")
    public ResponseEntity<FacultyProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody FacultyProfileRequest request) {
        return ResponseEntity.ok(facultyService.createOrUpdateProfile(userPrincipal.getId(), request));
    }

    @PostMapping("/attendance")
    @Operation(summary = "Mark class attendance", description = "Log student attendance records (PRESENT, ABSENT, LATE) for a specific course session and date.")
    @ApiResponse(responseCode = "200", description = "Successfully marked class attendance")
    @ApiResponse(responseCode = "400", description = "Course not assigned to this faculty, or student not enrolled")
    public ResponseEntity<Void> markAttendance(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AttendanceRecordRequest request) {
        facultyService.markAttendance(userPrincipal.getId(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/marks")
    @Operation(summary = "Enter student grades/marks", description = "Grade student performance in a specific course by uploading exams/tests scoring.")
    @ApiResponse(responseCode = "200", description = "Successfully recorded student grades")
    @ApiResponse(responseCode = "400", description = "Course not assigned to this faculty, or student not enrolled")
    public ResponseEntity<Void> enterMarks(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody MarksRequest request) {
        facultyService.enterMarks(userPrincipal.getId(), request);
        return ResponseEntity.ok().build();
    }
}
