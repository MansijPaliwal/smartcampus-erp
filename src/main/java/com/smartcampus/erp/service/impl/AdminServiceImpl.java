package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.dto.*;
import com.smartcampus.erp.entity.*;
import com.smartcampus.erp.exception.BadRequestException;
import com.smartcampus.erp.exception.ConflictException;
import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.exception.ValidationException;
import com.smartcampus.erp.repository.*;
import com.smartcampus.erp.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final CourseRepository courseRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(UserRepository userRepository,
                            StudentProfileRepository studentProfileRepository,
                            FacultyProfileRepository facultyProfileRepository,
                            CourseRepository courseRepository,
                            FeePaymentRepository feePaymentRepository,
                            EnrollmentRepository enrollmentRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.courseRepository = courseRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void validateUserCreation(String email, String password, String phone, Map<String, String> errors) {
        if (email == null || email.isBlank()) {
            errors.put("email", "Email is required");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.put("email", "Invalid email format");
        }

        if (password != null) {
            if (password.length() < 8) {
                errors.put("password", "Too weak");
            } else {
                boolean hasUpper = false;
                boolean hasDigit = false;
                boolean hasSpecial = false;
                for (char c : password.toCharArray()) {
                    if (Character.isUpperCase(c)) hasUpper = true;
                    else if (Character.isDigit(c)) hasDigit = true;
                    else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
                }
                if (!hasUpper || !hasDigit || !hasSpecial) {
                    errors.put("password", "Too weak");
                }
            }
        }

        if (phone != null && !phone.isBlank() && !phone.matches("^\\+?[0-9]{10,15}$")) {
            errors.put("phone", "Invalid phone number format");
        }
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        Map<String, String> errors = new HashMap<>();
        validateUserCreation(request.getEmail(), request.getPassword(), request.getPhone(), errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .enabled(request.isEnabled())
                .build();

        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String roleStr, Boolean enabled, String search, Pageable pageable) {
        Role role = null;
        if (roleStr != null && !roleStr.isBlank() && !roleStr.equalsIgnoreCase("ALL")) {
            try {
                role = Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid role filter
            }
        }
        Page<User> users = userRepository.findAllFiltered(role, enabled, search, pageable);
        return users.map(this::mapToUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + id));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + id));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use: " + request.getEmail());
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setEnabled(request.isEnabled());

        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + id));
        // Soft delete (set enabled=false, never hard delete)
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse changeUserRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + id));
        user.setRole(role);
        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    @Override
    @Transactional
    public void resetUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + id));

        Map<String, String> errors = new HashMap<>();
        validateUserCreation(user.getEmail(), newPassword, null, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Student profile methods
    @Override
    @Transactional
    public StudentProfileResponse createStudent(StudentUserRequest request) {
        Map<String, String> errors = new HashMap<>();
        validateUserCreation(request.getEmail(), request.getPassword(), request.getPhone(), errors);
        if (request.getRollNumber() == null || request.getRollNumber().isBlank()) {
            errors.put("rollNumber", "Roll number is required");
        }
        if (request.getSemester() == null) {
            errors.put("semester", "Semester is required");
        }
        if (request.getDob() == null) {
            errors.put("dob", "Date of birth is required");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use: " + request.getEmail());
        }
        if (studentProfileRepository.existsByRollNumber(request.getRollNumber())) {
            throw new ConflictException("Roll number is already in use: " + request.getRollNumber());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .phone(request.getPhone())
                .enabled(request.isEnabled())
                .build();
        User savedUser = userRepository.save(user);

        StudentProfile studentProfile = StudentProfile.builder()
                .user(savedUser)
                .rollNumber(request.getRollNumber())
                .department(request.getDepartment())
                .semester(request.getSemester())
                .dob(request.getDob())
                .phone(request.getPhone())
                .gender(request.getGender())
                .address(request.getAddress())
                .build();
        StudentProfile savedProfile = studentProfileRepository.save(studentProfile);

        return mapToStudentProfileResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentProfileResponse> getAllStudents() {
        return studentProfileRepository.findAll().stream()
                .map(this::mapToStudentProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StudentProfileResponse updateStudent(Long id, StudentUserRequest request) {
        StudentProfile profile = studentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for ID: " + id));
        User user = profile.getUser();

        Map<String, String> errors = new HashMap<>();
        validateUserCreation(request.getEmail(), null, request.getPhone(), errors);
        if (request.getRollNumber() == null || request.getRollNumber().isBlank()) {
            errors.put("rollNumber", "Roll number is required");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use: " + request.getEmail());
        }
        if (!profile.getRollNumber().equalsIgnoreCase(request.getRollNumber()) && studentProfileRepository.existsByRollNumber(request.getRollNumber())) {
            throw new ConflictException("Roll number is already in use: " + request.getRollNumber());
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setEnabled(request.isEnabled());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            errors = new HashMap<>();
            validateUserCreation(user.getEmail(), request.getPassword(), null, errors);
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        profile.setRollNumber(request.getRollNumber());
        profile.setDepartment(request.getDepartment());
        profile.setSemester(request.getSemester());
        profile.setDob(request.getDob());
        profile.setPhone(request.getPhone());
        profile.setGender(request.getGender());
        profile.setAddress(request.getAddress());
        StudentProfile saved = studentProfileRepository.save(profile);

        return mapToStudentProfileResponse(saved);
    }

    // Faculty profile methods
    @Override
    @Transactional
    public FacultyProfileResponse createFaculty(FacultyUserRequest request) {
        Map<String, String> errors = new HashMap<>();
        validateUserCreation(request.getEmail(), request.getPassword(), request.getPhone(), errors);
        if (request.getDesignation() == null || request.getDesignation().isBlank()) {
            errors.put("designation", "Designation is required");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.FACULTY)
                .phone(request.getPhone())
                .enabled(request.isEnabled())
                .build();
        User savedUser = userRepository.save(user);

        FacultyProfile facultyProfile = FacultyProfile.builder()
                .user(savedUser)
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .phone(request.getPhone())
                .joiningDate(request.getJoiningDate())
                .specialization(request.getSpecialization())
                .build();
        FacultyProfile savedProfile = facultyProfileRepository.save(facultyProfile);

        return mapToFacultyProfileResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyProfileResponse> getAllFaculty() {
        return facultyProfileRepository.findAll().stream()
                .map(this::mapToFacultyProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FacultyProfileResponse updateFaculty(Long id, FacultyUserRequest request) {
        FacultyProfile profile = facultyProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for ID: " + id));
        User user = profile.getUser();

        Map<String, String> errors = new HashMap<>();
        validateUserCreation(request.getEmail(), null, request.getPhone(), errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use: " + request.getEmail());
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setEnabled(request.isEnabled());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            errors = new HashMap<>();
            validateUserCreation(user.getEmail(), request.getPassword(), null, errors);
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        profile.setDepartment(request.getDepartment());
        profile.setDesignation(request.getDesignation());
        profile.setPhone(request.getPhone());
        profile.setJoiningDate(request.getJoiningDate());
        profile.setSpecialization(request.getSpecialization());
        FacultyProfile saved = facultyProfileRepository.save(profile);

        return mapToFacultyProfileResponse(saved);
    }

    // Profile self management
    @Override
    @Transactional(readOnly = true)
    public UserResponse getAdminProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateAdminProfile(String email, AdminProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use: " + request.getEmail());
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    @Override
    @Transactional
    public void changeAdminPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));

        Map<String, String> errors = new HashMap<>();
        validateUserCreation(user.getEmail(), newPassword, null, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkRollNumberAvailable(String rollNumber) {
        return !studentProfileRepository.existsByRollNumber(rollNumber);
    }

    // Existing methods
    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Course code is already in use: " + request.getCode());
        }

        FacultyProfile faculty = null;
        if (request.getFacultyId() != null) {
            faculty = facultyProfileRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found for ID: " + request.getFacultyId()));
        }

        Course course = Course.builder()
                .code(request.getCode())
                .title(request.getTitle())
                .credits(request.getCredits())
                .department(request.getDepartment())
                .faculty(faculty)
                .build();

        Course saved = courseRepository.save(course);
        return mapToCourseResponse(saved);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found for ID: " + id));

        if (!course.getCode().equalsIgnoreCase(request.getCode()) && courseRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Course code is already in use: " + request.getCode());
        }

        FacultyProfile faculty = null;
        if (request.getFacultyId() != null) {
            faculty = facultyProfileRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found for ID: " + request.getFacultyId()));
        }

        course.setCode(request.getCode());
        course.setTitle(request.getTitle());
        course.setCredits(request.getCredits());
        course.setDepartment(request.getDepartment());
        course.setFaculty(faculty);

        Course saved = courseRepository.save(course);
        return mapToCourseResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found for ID: " + id);
        }
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        long studentCount = studentProfileRepository.count();
        long facultyCount = facultyProfileRepository.count();
        long courseCount = courseRepository.count();

        List<FeePayment> pendingPayments = feePaymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .collect(Collectors.toList());
        double totalPendingFees = pendingPayments.stream()
                .mapToDouble(FeePayment::getAmount)
                .sum();

        return AdminDashboardStatsResponse.builder()
                .studentCount(studentCount)
                .facultyCount(facultyCount)
                .courseCount(courseCount)
                .totalPendingFees(totalPendingFees)
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .build();
    }

    private StudentProfileResponse mapToStudentProfileResponse(StudentProfile profile) {
        return StudentProfileResponse.builder()
                .id(profile.getId())
                .name(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .rollNumber(profile.getRollNumber())
                .department(profile.getDepartment())
                .semester(profile.getSemester())
                .dob(profile.getDob())
                .phone(profile.getPhone())
                .gender(profile.getGender())
                .address(profile.getAddress())
                .build();
    }

    private FacultyProfileResponse mapToFacultyProfileResponse(FacultyProfile profile) {
        return FacultyProfileResponse.builder()
                .id(profile.getId())
                .name(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .department(profile.getDepartment())
                .designation(profile.getDesignation())
                .phone(profile.getPhone())
                .joiningDate(profile.getJoiningDate())
                .specialization(profile.getSpecialization())
                .build();
    }

    private CourseResponse mapToCourseResponse(Course course) {
        String facultyName = course.getFaculty() != null ? course.getFaculty().getUser().getName() : "N/A";
        Long facultyId = course.getFaculty() != null ? course.getFaculty().getId() : null;
        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .title(course.getTitle())
                .credits(course.getCredits())
                .department(course.getDepartment())
                .facultyName(facultyName)
                .facultyId(facultyId)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(enrollment -> {
                    return EnrollmentResponse.builder()
                            .id(enrollment.getId())
                            .studentId(enrollment.getStudent().getId())
                            .studentName(enrollment.getStudent().getUser().getName())
                            .courseId(enrollment.getCourse().getId())
                            .courseCode(enrollment.getCourse().getCode())
                            .courseTitle(enrollment.getCourse().getTitle())
                            .enrollmentDate(enrollment.getEnrollmentDate())
                            .status(enrollment.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
