package com.smartcampus.erp.controller;

import com.smartcampus.erp.dto.*;
import com.smartcampus.erp.entity.Role;
import com.smartcampus.erp.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administration management APIs for users, courses, system diagnostics, and enrollments")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users")
    @Operation(summary = "Create any user", description = "Register a general STUDENT, FACULTY, or ADMIN user account.")
    @ApiResponse(responseCode = "201", description = "Successfully created user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @GetMapping("/users")
    @Operation(summary = "List all users", description = "Retrieve a paginated list of all users, optionally filtered by role and status.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users page")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(adminService.getAllUsers(role, enabled, search, pageable));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve specific details of a user using their unique ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the user details")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user details", description = "Modify a user's name, email, role, or active status.")
    @ApiResponse(responseCode = "200", description = "Successfully updated the user details")
    @ApiResponse(responseCode = "400", description = "Invalid request payload or email already taken")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user", description = "Soft delete a user by setting enabled=false.")
    @ApiResponse(responseCode = "204", description = "Successfully deleted user and profiles")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Change user role", description = "Update the security authorization role of a user.")
    @ApiResponse(responseCode = "200", description = "Successfully changed role")
    public ResponseEntity<UserResponse> changeUserRole(@PathVariable Long id, @RequestParam Role role) {
        return ResponseEntity.ok(adminService.changeUserRole(id, role));
    }

    @PatchMapping("/users/{id}/reset-password")
    @Operation(summary = "Admin resets a user's password", description = "Directly reset a user's login password.")
    @ApiResponse(responseCode = "204", description = "Password successfully reset")
    public ResponseEntity<Void> resetUserPassword(@PathVariable Long id, @RequestParam String newPassword) {
        adminService.resetUserPassword(id, newPassword);
        return ResponseEntity.noContent().build();
    }

    // Student-specific User Creation
    @PostMapping("/students")
    @Operation(summary = "Create student", description = "Register a new student account and create their profile record in a single transaction.")
    @ApiResponse(responseCode = "201", description = "Successfully created student and profile")
    public ResponseEntity<StudentProfileResponse> createStudent(@Valid @RequestBody StudentUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createStudent(request));
    }

    @GetMapping("/students")
    @Operation(summary = "List all students", description = "Retrieve list of all student accounts along with profile details.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved students")
    public ResponseEntity<List<StudentProfileResponse>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    @PutMapping("/students/{id}")
    @Operation(summary = "Update student", description = "Modify a student account and their associated profile.")
    @ApiResponse(responseCode = "200", description = "Successfully updated student and profile")
    public ResponseEntity<StudentProfileResponse> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUserRequest request) {
        return ResponseEntity.ok(adminService.updateStudent(id, request));
    }

    // Faculty-specific User Creation
    @PostMapping("/faculty")
    @Operation(summary = "Create faculty", description = "Register a new faculty account and create their profile record.")
    @ApiResponse(responseCode = "201", description = "Successfully created faculty and profile")
    public ResponseEntity<FacultyProfileResponse> createFaculty(@Valid @RequestBody FacultyUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createFaculty(request));
    }

    @GetMapping("/faculty")
    @Operation(summary = "List all faculty", description = "Retrieve list of all faculty accounts along with profile details.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved faculty")
    public ResponseEntity<List<FacultyProfileResponse>> getAllFaculty() {
        return ResponseEntity.ok(adminService.getAllFaculty());
    }

    @PutMapping("/faculty/{id}")
    @Operation(summary = "Update faculty", description = "Modify a faculty account and their profile.")
    @ApiResponse(responseCode = "200", description = "Successfully updated faculty")
    public ResponseEntity<FacultyProfileResponse> updateFaculty(@PathVariable Long id, @Valid @RequestBody FacultyUserRequest request) {
        return ResponseEntity.ok(adminService.updateFaculty(id, request));
    }

    // Admin Profile API
    @GetMapping("/profile")
    @Operation(summary = "Get admin's own profile", description = "Retrieve the logged-in admin's account settings details.")
    public ResponseEntity<UserResponse> getAdminProfile(Principal principal) {
        return ResponseEntity.ok(adminService.getAdminProfile(principal.getName()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update admin's own profile", description = "Modify the logged-in admin's profile name or phone.")
    public ResponseEntity<UserResponse> updateAdminProfile(Principal principal, @Valid @RequestBody AdminProfileRequest request) {
        return ResponseEntity.ok(adminService.updateAdminProfile(principal.getName(), request));
    }

    @PatchMapping("/profile/password")
    @Operation(summary = "Admin changes own password", description = "Directly update the logged-in admin's login password.")
    public ResponseEntity<Void> changeAdminPassword(Principal principal, @RequestParam String newPassword) {
        adminService.changeAdminPassword(principal.getName(), newPassword);
        return ResponseEntity.noContent().build();
    }

    // Uniqueness Checks
    @GetMapping("/users/check-email")
    @Operation(summary = "Check email availability", description = "Verify if a user email address is unique and available.")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        Map<String, Boolean> res = new HashMap<>();
        res.put("available", adminService.checkEmailAvailable(email));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/students/check-roll")
    @Operation(summary = "Check roll number availability", description = "Verify if a student roll number is unique.")
    public ResponseEntity<Map<String, Boolean>> checkRoll(@RequestParam String rollNumber) {
        Map<String, Boolean> res = new HashMap<>();
        res.put("available", adminService.checkRollNumberAvailable(rollNumber));
        return ResponseEntity.ok(res);
    }

    // Courses and Statistics
    @PostMapping("/courses")
    @Operation(summary = "Create course", description = "Register a new course and assign a faculty member as the instructor.")
    @ApiResponse(responseCode = "201", description = "Successfully created the course")
    @ApiResponse(responseCode = "400", description = "Course code is already in use")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createCourse(request));
    }

    @PutMapping("/courses/{id}")
    @Operation(summary = "Update course details", description = "Modify a course's code, title, credits, department, or instructor.")
    @ApiResponse(responseCode = "200", description = "Successfully updated the course details")
    @ApiResponse(responseCode = "400", description = "Course code is already taken")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(adminService.updateCourse(id, request));
    }

    @DeleteMapping("/courses/{id}")
    @Operation(summary = "Delete course", description = "Remove a course by its ID.")
    @ApiResponse(responseCode = "204", description = "Successfully deleted the course")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        adminService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard statistics", description = "Calculate totals for students, faculty, courses, and pending tuition dues.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    public ResponseEntity<AdminDashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get data-driven admin dashboard stats", description = "Retrieve total enrolled students, total pending fees, and top 3 recent system notifications.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    public ResponseEntity<AdminDashboardStatsResponse> getDashboardStatsNew() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/enrollments")
    @Operation(summary = "Get all enrollments", description = "Retrieve list of all student course enrollments in the system.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved enrollment history")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        return ResponseEntity.ok(adminService.getAllEnrollments());
    }
}
