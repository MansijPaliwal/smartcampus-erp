package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.dto.*;
import com.smartcampus.erp.entity.*;
import com.smartcampus.erp.exception.BadRequestException;
import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.repository.*;
import com.smartcampus.erp.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;
    private final FeePaymentRepository feePaymentRepository;

    public StudentServiceImpl(StudentProfileRepository studentProfileRepository,
                              UserRepository userRepository,
                              EnrollmentRepository enrollmentRepository,
                              AttendanceRepository attendanceRepository,
                              MarksRepository marksRepository,
                              FeePaymentRepository feePaymentRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.marksRepository = marksRepository;
        this.feePaymentRepository = feePaymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(Long studentUserId) {
        StudentProfile profile = studentProfileRepository.findById(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + studentUserId));
        return mapToProfileResponse(profile);
    }

    @Override
    @Transactional
    public StudentProfileResponse createOrUpdateProfile(Long studentUserId, StudentProfileRequest request) {
        User user = userRepository.findById(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + studentUserId));

        if (user.getRole() != Role.STUDENT) {
            throw new BadRequestException("User role is not STUDENT");
        }

        StudentProfile profile = studentProfileRepository.findById(studentUserId).orElse(null);
        if (profile == null) {
            profile = new StudentProfile();
            profile.setUser(user);
        }

        profile.setRollNumber(request.getRollNumber());
        profile.setDepartment(request.getDepartment());
        profile.setSemester(request.getSemester());
        profile.setDob(request.getDob());
        profile.setPhone(request.getPhone());

        StudentProfile saved = studentProfileRepository.save(profile);
        return mapToProfileResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getEnrolledCourses(Long studentUserId) {
        if (!studentProfileRepository.existsById(studentUserId)) {
            throw new ResourceNotFoundException("Student profile not found for ID: " + studentUserId);
        }
        
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(studentUserId, EnrollmentStatus.ACTIVE);
        return enrollments.stream()
                .map(enrollment -> {
                    Course course = enrollment.getCourse();
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
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendance(Long studentUserId, Long courseId) {
        if (!studentProfileRepository.existsById(studentUserId)) {
            throw new ResourceNotFoundException("Student profile not found for ID: " + studentUserId);
        }

        List<Attendance> attendances;
        if (courseId != null) {
            attendances = attendanceRepository.findByStudentIdAndCourseId(studentUserId, courseId);
        } else {
            attendances = attendanceRepository.findByStudentId(studentUserId);
        }

        return attendances.stream()
                .map(att -> AttendanceResponse.builder()
                        .id(att.getId())
                        .courseCode(att.getCourse().getCode())
                        .courseTitle(att.getCourse().getTitle())
                        .date(att.getDate())
                        .status(att.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarksResponse> getMarks(Long studentUserId) {
        if (!studentProfileRepository.existsById(studentUserId)) {
            throw new ResourceNotFoundException("Student profile not found for ID: " + studentUserId);
        }

        List<Marks> marksList = marksRepository.findByStudentId(studentUserId);
        return marksList.stream()
                .map(m -> MarksResponse.builder()
                        .id(m.getId())
                        .courseCode(m.getCourse().getCode())
                        .courseTitle(m.getCourse().getTitle())
                        .examType(m.getExamType())
                        .marksObtained(m.getMarksObtained())
                        .maxMarks(m.getMaxMarks())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentFeeStatusResponse getFeeStatus(Long studentUserId) {
        if (!studentProfileRepository.existsById(studentUserId)) {
            throw new ResourceNotFoundException("Student profile not found for ID: " + studentUserId);
        }

        List<FeePayment> payments = feePaymentRepository.findByStudentId(studentUserId);
        
        java.math.BigDecimal totalDues = payments.stream()
                .map(FeePayment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(FeePayment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal pendingDues = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(FeePayment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        List<FeePaymentResponse> history = payments.stream()
                .map(p -> FeePaymentResponse.builder()
                        .id(p.getId())
                        .amount(p.getAmount())
                        .paymentDate(p.getPaymentDate())
                        .status(p.getStatus().name())
                        .transactionId(p.getTransactionId())
                        .studentId(p.getStudent() != null ? p.getStudent().getId() : null)
                        .studentName(p.getStudent() != null && p.getStudent().getUser() != null ? p.getStudent().getUser().getName() : null)
                        .paymentMethod(p.getPaymentMethod())
                        .build())
                .collect(Collectors.toList());

        return StudentFeeStatusResponse.builder()
                .totalDues(totalDues)
                .totalPaid(totalPaid)
                .pendingDues(pendingDues)
                .paymentHistory(history)
                .build();
    }

    private StudentProfileResponse mapToProfileResponse(StudentProfile profile) {
        return StudentProfileResponse.builder()
                .id(profile.getId())
                .name(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .rollNumber(profile.getRollNumber())
                .department(profile.getDepartment())
                .semester(profile.getSemester())
                .dob(profile.getDob())
                .phone(profile.getPhone())
                .build();
    }
}
