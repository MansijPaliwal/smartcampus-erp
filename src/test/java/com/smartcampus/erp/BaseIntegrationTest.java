package com.smartcampus.erp;

import com.smartcampus.erp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @MockBean
    protected RedisConnectionFactory redisConnectionFactory;

    @MockBean
    protected org.springframework.data.redis.connection.ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @MockBean(name = "redisTemplate")
    protected RedisTemplate<String, String> redisTemplate;

    @MockBean
    protected JavaMailSender javaMailSender;

    @Autowired protected UserRepository userRepository;
    @Autowired protected StudentProfileRepository studentProfileRepository;
    @Autowired protected FacultyProfileRepository facultyProfileRepository;
    @Autowired protected CourseRepository courseRepository;
    @Autowired protected EnrollmentRepository enrollmentRepository;
    @Autowired protected AttendanceRepository attendanceRepository;
    @Autowired protected AssignmentRepository assignmentRepository;
    @Autowired protected AssignmentSubmissionRepository submissionRepository;
    @Autowired protected MarksRepository marksRepository;
    @Autowired protected FeePaymentRepository feePaymentRepository;
    @Autowired protected NotificationRepository notificationRepository;

    protected void cleanupDatabase() {
        attendanceRepository.deleteAll();
        submissionRepository.deleteAll();
        assignmentRepository.deleteAll();
        marksRepository.deleteAll();
        enrollmentRepository.deleteAll();
        feePaymentRepository.deleteAll();
        notificationRepository.deleteAll();
        courseRepository.deleteAll();
        studentProfileRepository.deleteAll();
        facultyProfileRepository.deleteAll();
        userRepository.deleteAll();
    }
}
