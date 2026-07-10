package com.smartcampus.erp;

import com.smartcampus.erp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.chat.client.ChatClient;
import org.mockito.Mockito;

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

    @MockBean
    protected ChatModel chatModel;

    @MockBean
    protected ChatClient.Builder chatClientBuilder;

    @MockBean
    protected VectorStore vectorStore;

    @MockBean
    protected EmbeddingModel embeddingModel;

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
    @Autowired protected ExamFormRepository examFormRepository;

    @BeforeEach
    public void setUpBaseMocks() {
        // Stub simple ChatModel call(String)
        Mockito.when(chatModel.call(Mockito.anyString()))
               .thenReturn("Tactical GPA strategy study optimization blueprint focus.");

        // Stub complex ChatModel call(Prompt)
        ChatResponse mockChatResponse = Mockito.mock(ChatResponse.class);
        Generation mockGeneration = Mockito.mock(Generation.class);
        AssistantMessage mockAssistantMessage = new AssistantMessage("Tactical study optimization advisor response.");

        Mockito.when(mockGeneration.getOutput()).thenReturn(mockAssistantMessage);
        Mockito.when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        Mockito.when(chatModel.call(Mockito.any(Prompt.class)))
               .thenReturn(mockChatResponse);

        // Stub fluent ChatClient mock chain using Mockito deep stubs to avoid type mismatch on fluent spec classes
        ChatClient mockChatClient = Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(chatClientBuilder.build()).thenReturn(mockChatClient);
        Mockito.when(mockChatClient.prompt()
                     .system(Mockito.anyString())
                     .user(Mockito.anyString())
                     .call()
                     .content())
               .thenReturn("Tactical study optimization advisor response.");
    }

    protected void cleanupDatabase() {
        attendanceRepository.deleteAllInBatch();
        submissionRepository.deleteAllInBatch();
        assignmentRepository.deleteAllInBatch();
        marksRepository.deleteAllInBatch();
        enrollmentRepository.deleteAllInBatch();
        feePaymentRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
        examFormRepository.deleteAllInBatch();
        courseRepository.deleteAllInBatch();
        studentProfileRepository.deleteAllInBatch();
        facultyProfileRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }
}
