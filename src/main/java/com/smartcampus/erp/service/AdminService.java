package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.*;
import com.smartcampus.erp.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminService {
    // User management
    UserResponse createUser(UserRequest request);
    Page<UserResponse> getAllUsers(String role, Boolean enabled, String search, Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id); // Soft delete (set enabled=false)
    UserResponse changeUserRole(Long id, Role role);
    void resetUserPassword(Long id, String newPassword);

    // Student profile endpoints
    StudentProfileResponse createStudent(StudentUserRequest request);
    List<StudentProfileResponse> getAllStudents();
    StudentProfileResponse updateStudent(Long id, StudentUserRequest request);

    // Faculty profile endpoints
    FacultyProfileResponse createFaculty(FacultyUserRequest request);
    List<FacultyProfileResponse> getAllFaculty();
    FacultyProfileResponse updateFaculty(Long id, FacultyUserRequest request);

    // Profile self management
    UserResponse getAdminProfile(String email);
    UserResponse updateAdminProfile(String email, AdminProfileRequest request);
    void changeAdminPassword(String email, String newPassword);

    // Check availability
    boolean checkEmailAvailable(String email);
    boolean checkRollNumberAvailable(String rollNumber);

    // Courses and statistics
    CourseResponse createCourse(CourseRequest request);
    CourseResponse updateCourse(Long id, CourseRequest request);
    void deleteCourse(Long id);
    AdminDashboardStatsResponse getDashboardStats();
    List<EnrollmentResponse> getAllEnrollments();
}
