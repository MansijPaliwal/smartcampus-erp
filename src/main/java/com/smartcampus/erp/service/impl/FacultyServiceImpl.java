package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.dto.AttendanceRecordRequest;
import com.smartcampus.erp.dto.FacultyProfileRequest;
import com.smartcampus.erp.dto.FacultyProfileResponse;
import com.smartcampus.erp.dto.MarksRequest;
import com.smartcampus.erp.entity.*;
import com.smartcampus.erp.exception.BadRequestException;
import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.exception.UnauthorizedException;
import com.smartcampus.erp.repository.*;
import com.smartcampus.erp.service.FacultyService;
import com.smartcampus.erp.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyProfileRepository facultyProfileRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;
    private final NotificationService notificationService;

    public FacultyServiceImpl(FacultyProfileRepository facultyProfileRepository,
                              UserRepository userRepository,
                              CourseRepository courseRepository,
                              StudentProfileRepository studentProfileRepository,
                              AttendanceRepository attendanceRepository,
                              MarksRepository marksRepository,
                              NotificationService notificationService) {
        this.facultyProfileRepository = facultyProfileRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.attendanceRepository = attendanceRepository;
        this.marksRepository = marksRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyProfileResponse getProfile(Long facultyUserId) {
        FacultyProfile profile = facultyProfileRepository.findById(facultyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + facultyUserId));
        return mapToProfileResponse(profile);
    }

    @Override
    @Transactional
    public FacultyProfileResponse createOrUpdateProfile(Long facultyUserId, FacultyProfileRequest request) {
        User user = userRepository.findById(facultyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + facultyUserId));

        if (user.getRole() != Role.FACULTY) {
            throw new BadRequestException("User role is not FACULTY");
        }

        FacultyProfile profile = facultyProfileRepository.findById(facultyUserId).orElse(null);
        if (profile == null) {
            profile = new FacultyProfile();
            profile.setUser(user);
        }

        profile.setDepartment(request.getDepartment());
        profile.setDesignation(request.getDesignation());

        FacultyProfile saved = facultyProfileRepository.save(profile);
        return mapToProfileResponse(saved);
    }

    @Override
    @Transactional
    public void markAttendance(Long facultyUserId, AttendanceRecordRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found for ID: " + request.getCourseId()));

        if (course.getFaculty() == null || !course.getFaculty().getId().equals(facultyUserId)) {
            throw new UnauthorizedException("Faculty is not authorized to mark attendance for this course");
        }

        for (var record : request.getRecords()) {
            StudentProfile student = studentProfileRepository.findById(record.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for ID: " + record.getStudentId()));

            AttendanceStatus status;
            try {
                status = AttendanceStatus.valueOf(record.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid attendance status: " + record.getStatus());
            }

            Optional<Attendance> existingOpt = attendanceRepository.findByStudentIdAndCourseIdAndDate(
                    student.getId(), course.getId(), request.getDate());

            if (existingOpt.isPresent()) {
                Attendance existing = existingOpt.get();
                existing.setStatus(status);
                attendanceRepository.save(existing);
            } else {
                Attendance attendance = Attendance.builder()
                        .course(course)
                        .student(student)
                        .date(request.getDate())
                        .status(status)
                        .build();
                attendanceRepository.save(attendance);
            }
        }
    }

    @Override
    @Transactional
    public void enterMarks(Long facultyUserId, MarksRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found for ID: " + request.getCourseId()));

        if (course.getFaculty() == null || !course.getFaculty().getId().equals(facultyUserId)) {
            throw new UnauthorizedException("Faculty is not authorized to enter marks for this course");
        }

        StudentProfile student = studentProfileRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for ID: " + request.getStudentId()));

        if (request.getMarksObtained() > request.getMaxMarks()) {
            throw new BadRequestException("Marks obtained cannot exceed maximum marks");
        }

        Optional<Marks> existingOpt = marksRepository.findByStudentIdAndCourseIdAndExamType(
                student.getId(), course.getId(), request.getExamType());

        Marks marks;
        if (existingOpt.isPresent()) {
            marks = existingOpt.get();
            marks.setMarksObtained(request.getMarksObtained());
            marks.setMaxMarks(request.getMaxMarks());
        } else {
            marks = Marks.builder()
                    .course(course)
                    .student(student)
                    .examType(request.getExamType())
                    .marksObtained(request.getMarksObtained())
                    .maxMarks(request.getMaxMarks())
                    .build();
        }

        marksRepository.save(marks);

        // Auto-create notification for student
        String msg = String.format("Your marks for %s (%s) have been updated: %.2f / %.2f",
                course.getTitle(), request.getExamType(), request.getMarksObtained(), request.getMaxMarks());
        notificationService.createNotification(student.getId(), "Marks Published", msg);
    }

    private FacultyProfileResponse mapToProfileResponse(FacultyProfile profile) {
        return FacultyProfileResponse.builder()
                .id(profile.getId())
                .name(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .department(profile.getDepartment())
                .designation(profile.getDesignation())
                .build();
    }
}
