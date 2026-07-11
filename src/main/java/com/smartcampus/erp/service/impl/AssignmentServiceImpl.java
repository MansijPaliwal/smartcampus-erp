package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.dto.*;
import com.smartcampus.erp.entity.*;
import com.smartcampus.erp.exception.BadRequestException;
import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.exception.UnauthorizedException;
import com.smartcampus.erp.repository.*;
import com.smartcampus.erp.service.AssignmentService;
import com.smartcampus.erp.service.FileStorageService;
import com.smartcampus.erp.service.NotificationService;
import com.smartcampus.erp.service.ProctoringService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final ProctoringService proctoringService;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository,
                                 AssignmentSubmissionRepository submissionRepository,
                                 CourseRepository courseRepository,
                                 StudentProfileRepository studentProfileRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 FileStorageService fileStorageService,
                                 NotificationService notificationService,
                                 ProctoringService proctoringService) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
        this.proctoringService = proctoringService;
    }

    @Override
    @Transactional
    public AssignmentResponse createAssignment(Long facultyUserId, AssignmentRequest request) {
        var course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found for ID: " + request.getCourseId()));

        if (course.getFaculty() == null || !course.getFaculty().getId().equals(facultyUserId)) {
            throw new UnauthorizedException("Faculty is not authorized to create assignments for this course");
        }

        var assignment = Assignment.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .maxMarks(request.getMaxMarks())
                .build();

        var saved = assignmentRepository.save(assignment);
        return mapToAssignmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getCourseAssignments(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found for ID: " + courseId);
        }
        return assignmentRepository.findByCourseId(courseId).stream()
                .map(this::mapToAssignmentResponse)
                .toList();
    }

    @Override
    @Transactional
    public SubmissionResponse submitAssignment(Long studentUserId, Long assignmentId, MultipartFile file, String ipAddress, Integer completionTimeSeconds) {
        var student = studentProfileRepository.findById(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user ID: " + studentUserId));

        var assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found for ID: " + assignmentId));

        // Validate student enrollment
        var enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentUserId, assignment.getCourse().getId())
                .orElseThrow(() -> new BadRequestException("Student is not enrolled in the course for this assignment"));

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new BadRequestException("Student enrollment is not active");
        }

        // Upload and store the file
        var uniqueFileName = fileStorageService.storeFile(file);

        var existingOpt = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentUserId);

        AssignmentSubmission submission;
        if (existingOpt.isPresent()) {
            submission = existingOpt.get();
            submission.setFileUrl(uniqueFileName);
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setIpAddress(ipAddress);
            submission.setCompletionTimeSeconds(completionTimeSeconds);
        } else {
            submission = AssignmentSubmission.builder()
                    .assignment(assignment)
                    .student(student)
                    .fileUrl(uniqueFileName)
                    .submittedAt(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .completionTimeSeconds(completionTimeSeconds)
                    .build();
        }

        // Save submission and run the automated proctoring analysis
        var saved = submissionRepository.save(submission);
        proctoringService.analyzeSubmissionIntegrity(saved);

        return mapToSubmissionResponse(saved);
    }

    @Override
    @Transactional
    public SubmissionResponse gradeSubmission(Long facultyUserId, Long submissionId, GradeSubmissionRequest request) {
        var submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found for ID: " + submissionId));

        var assignment = submission.getAssignment();
        var course = assignment.getCourse();

        if (course.getFaculty() == null || !course.getFaculty().getId().equals(facultyUserId)) {
            throw new UnauthorizedException("Faculty is not authorized to grade this submission");
        }

        if (request.getMarksObtained().compareTo(assignment.getMaxMarks()) > 0) {
            throw new BadRequestException("Marks obtained cannot exceed maximum assignment marks of: " + assignment.getMaxMarks());
        }

        submission.setMarksObtained(request.getMarksObtained());
        var saved = submissionRepository.save(submission);

        // Notify student
        var msg = String.format("Your submission for %s has been graded: %.2f / %.2f.",
                assignment.getTitle(), request.getMarksObtained().doubleValue(), assignment.getMaxMarks().doubleValue());
        notificationService.createNotification(submission.getStudent().getId(), "Assignment Graded", msg);

        return mapToSubmissionResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getAssignmentSubmissions(Long facultyUserId, Long assignmentId) {
        var assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found for ID: " + assignmentId));

        var course = assignment.getCourse();
        if (course.getFaculty() == null || !course.getFaculty().getId().equals(facultyUserId)) {
            throw new UnauthorizedException("Faculty is not authorized to view submissions for this assignment");
        }

        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(this::mapToSubmissionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getStudentSubmissions(Long studentUserId) {
        if (!studentProfileRepository.existsById(studentUserId)) {
            throw new ResourceNotFoundException("Student profile not found for ID: " + studentUserId);
        }
        return submissionRepository.findByStudentId(studentUserId).stream()
                .map(this::mapToSubmissionResponse)
                .toList();
    }

    private AssignmentResponse mapToAssignmentResponse(Assignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourse().getId())
                .courseTitle(assignment.getCourse().getTitle())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .maxMarks(assignment.getMaxMarks())
                .build();
    }

    private SubmissionResponse mapToSubmissionResponse(AssignmentSubmission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .assignmentTitle(submission.getAssignment().getTitle())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getUser().getName())
                .fileUrl(submission.getFileUrl())
                .submittedAt(submission.getSubmittedAt())
                .marksObtained(submission.getMarksObtained())
                .integrityScore(submission.getIntegrityScore())
                .build();
    }
}
