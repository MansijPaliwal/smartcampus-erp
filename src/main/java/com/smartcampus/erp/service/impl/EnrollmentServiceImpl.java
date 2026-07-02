package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.dto.EnrollmentResponse;
import com.smartcampus.erp.entity.*;
import com.smartcampus.erp.exception.BadRequestException;
import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.repository.CourseRepository;
import com.smartcampus.erp.repository.EnrollmentRepository;
import com.smartcampus.erp.repository.StudentProfileRepository;
import com.smartcampus.erp.service.EnrollmentService;
import com.smartcampus.erp.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                 StudentProfileRepository studentProfileRepository,
                                 CourseRepository courseRepository,
                                 NotificationService notificationService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.courseRepository = courseRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public EnrollmentResponse enroll(Long studentUserId, Long courseId) {
        StudentProfile student = studentProfileRepository.findById(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + studentUserId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found for ID: " + courseId));

        Optional<Enrollment> existingOpt = enrollmentRepository.findByStudentIdAndCourseId(studentUserId, courseId);

        Enrollment enrollment;
        if (existingOpt.isPresent()) {
            enrollment = existingOpt.get();
            if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                throw new BadRequestException("Student is already enrolled in this course");
            }
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setEnrollmentDate(LocalDate.now());
        } else {
            enrollment = Enrollment.builder()
                    .student(student)
                    .course(course)
                    .enrollmentDate(LocalDate.now())
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
        }

        Enrollment saved = enrollmentRepository.save(enrollment);

        // Notify student
        String msg = String.format("You have successfully enrolled in %s (%s).", course.getTitle(), course.getCode());
        notificationService.createNotification(studentUserId, "Course Enrolled", msg);

        return mapToEnrollmentResponse(saved);
    }

    @Override
    @Transactional
    public EnrollmentResponse drop(Long studentUserId, Long courseId) {
        StudentProfile student = studentProfileRepository.findById(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + studentUserId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found for ID: " + courseId));

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentUserId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for student and course"));

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new BadRequestException("Enrollment is already in DROPPED status");
        }

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        Enrollment saved = enrollmentRepository.save(enrollment);

        // Notify student
        String msg = String.format("You have dropped the course %s (%s).", course.getTitle(), course.getCode());
        notificationService.createNotification(studentUserId, "Course Dropped", msg);

        return mapToEnrollmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollments(Long studentUserId) {
        if (!studentProfileRepository.existsById(studentUserId)) {
            throw new ResourceNotFoundException("Student profile not found for ID: " + studentUserId);
        }
        return enrollmentRepository.findByStudentId(studentUserId).stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getCourseEnrollments(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found for ID: " + courseId);
        }
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }

    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
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
    }
}
