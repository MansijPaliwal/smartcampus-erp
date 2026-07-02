package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.StudentFeeStatusResponse;
import com.smartcampus.erp.dto.StudentProfileRequest;
import com.smartcampus.erp.dto.StudentProfileResponse;
import com.smartcampus.erp.entity.*;
import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.repository.*;
import com.smartcampus.erp.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private MarksRepository marksRepository;
    @Mock private FeePaymentRepository feePaymentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private User studentUser;
    private StudentProfile studentProfile;

    @BeforeEach
    void setUp() {
        studentUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@test.com")
                .role(Role.STUDENT)
                .enabled(true)
                .build();

        studentProfile = StudentProfile.builder()
                .id(1L)
                .user(studentUser)
                .rollNumber("S101")
                .department("CS")
                .semester(3)
                .dob(LocalDate.of(2002, 5, 10))
                .phone("1234567890")
                .build();
    }

    @Test
    void getProfile_success() {
        when(studentProfileRepository.findById(1L)).thenReturn(Optional.of(studentProfile));
        StudentProfileResponse response = studentService.getProfile(1L);
        assertNotNull(response);
        assertEquals("S101", response.getRollNumber());
    }

    @Test
    void getProfile_notFound_throwsException() {
        when(studentProfileRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> studentService.getProfile(2L));
    }

    @Test
    void createOrUpdateProfile_success() {
        StudentProfileRequest request = StudentProfileRequest.builder()
                .rollNumber("S101")
                .department("CS")
                .semester(3)
                .dob(LocalDate.of(2002, 5, 10))
                .phone("1234567890")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(studentUser));
        when(studentProfileRepository.findById(1L)).thenReturn(Optional.empty());
        when(studentProfileRepository.save(any())).thenReturn(studentProfile);

        StudentProfileResponse response = studentService.createOrUpdateProfile(1L, request);
        assertNotNull(response);
        assertEquals("S101", response.getRollNumber());
        verify(studentProfileRepository, times(1)).save(any());
    }

    @Test
    void getFeeStatus_success() {
        when(studentProfileRepository.existsById(1L)).thenReturn(true);
        FeePayment p1 = FeePayment.builder().amount(1500.0).status(PaymentStatus.PAID).paymentDate(LocalDateTime.now()).build();
        FeePayment p2 = FeePayment.builder().amount(500.0).status(PaymentStatus.PENDING).build();
        when(feePaymentRepository.findByStudentId(1L)).thenReturn(Arrays.asList(p1, p2));

        StudentFeeStatusResponse status = studentService.getFeeStatus(1L);
        assertNotNull(status);
        assertEquals(2000.0, status.getTotalDues());
        assertEquals(1500.0, status.getTotalPaid());
        assertEquals(500.0, status.getPendingDues());
        assertEquals(2, status.getPaymentHistory().size());
    }
}
