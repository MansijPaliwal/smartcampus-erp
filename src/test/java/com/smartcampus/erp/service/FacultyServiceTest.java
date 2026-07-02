package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.FacultyProfileRequest;
import com.smartcampus.erp.dto.FacultyProfileResponse;
import com.smartcampus.erp.entity.FacultyProfile;
import com.smartcampus.erp.entity.Role;
import com.smartcampus.erp.entity.User;
import com.smartcampus.erp.repository.*;
import com.smartcampus.erp.service.impl.FacultyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FacultyServiceTest {

    @Mock private FacultyProfileRepository facultyProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private MarksRepository marksRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private FacultyServiceImpl facultyService;

    private User facultyUser;
    private FacultyProfile facultyProfile;

    @BeforeEach
    void setUp() {
        facultyUser = User.builder()
                .id(2L)
                .name("Dr. Smith")
                .email("smith@test.com")
                .role(Role.FACULTY)
                .enabled(true)
                .build();

        facultyProfile = FacultyProfile.builder()
                .id(2L)
                .user(facultyUser)
                .department("CS")
                .designation("Professor")
                .build();
    }

    @Test
    void getProfile_success() {
        when(facultyProfileRepository.findById(2L)).thenReturn(Optional.of(facultyProfile));
        FacultyProfileResponse response = facultyService.getProfile(2L);
        assertNotNull(response);
        assertEquals("CS", response.getDepartment());
    }

    @Test
    void createOrUpdateProfile_success() {
        FacultyProfileRequest request = FacultyProfileRequest.builder()
                .department("CS")
                .designation("Professor")
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(facultyUser));
        when(facultyProfileRepository.findById(2L)).thenReturn(Optional.empty());
        when(facultyProfileRepository.save(any())).thenReturn(facultyProfile);

        FacultyProfileResponse response = facultyService.createOrUpdateProfile(2L, request);
        assertNotNull(response);
        assertEquals("Dr. Smith", response.getName());
    }
}
