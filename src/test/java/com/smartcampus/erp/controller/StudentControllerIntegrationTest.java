package com.smartcampus.erp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.erp.BaseIntegrationTest;
import com.smartcampus.erp.dto.AuthResponse;
import com.smartcampus.erp.dto.RegisterRequest;
import com.smartcampus.erp.dto.StudentProfileRequest;
import com.smartcampus.erp.entity.Role;
import com.smartcampus.erp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StudentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cleanupDatabase();
    }

    @Test
    void studentProfileFlow_integrationTest() throws Exception {
        // 1. Register student
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Student John")
                .email("john@smartcampus.com")
                .password("password123")
                .role(Role.STUDENT)
                .build();

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String regResponseBody = regResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(regResponseBody, AuthResponse.class);
        String token = "Bearer " + authResponse.getToken();

        // 2. Put Student Profile (Create)
        StudentProfileRequest profileRequest = StudentProfileRequest.builder()
                .rollNumber("ROLL-001")
                .department("Information Technology")
                .semester(1)
                .dob(LocalDate.of(2004, 8, 12))
                .phone("+1234567890")
                .build();

        mockMvc.perform(put("/api/students/profile")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollNumber").value("ROLL-001"))
                .andExpect(jsonPath("$.department").value("Information Technology"))
                .andExpect(jsonPath("$.semester").value(1));

        // 3. Get Student Profile
        mockMvc.perform(get("/api/students/profile")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollNumber").value("ROLL-001"))
                .andExpect(jsonPath("$.name").value("Student John"))
                .andExpect(jsonPath("$.email").value("john@smartcampus.com"));
    }

    @Test
    void getAiInsights_integrationTest() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Student Advisor")
                .email("advisor@smartcampus.com")
                .password("password123")
                .role(Role.STUDENT)
                .build();

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String regResponseBody = regResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(regResponseBody, AuthResponse.class);
        String token = "Bearer " + authResponse.getToken();

        StudentProfileRequest profileRequest = StudentProfileRequest.builder()
                .rollNumber("ROLL-ADVISOR")
                .department("Information Technology")
                .semester(1)
                .dob(LocalDate.of(2004, 8, 12))
                .phone("+1234567890")
                .build();

        mockMvc.perform(put("/api/students/profile")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/students/gpa/insights")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiInsight").exists());
    }

    @Test
    void askAssistant_integrationTest() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Chat Student")
                .email("chat@smartcampus.com")
                .password("password123")
                .role(Role.STUDENT)
                .build();

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String regResponseBody = regResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(regResponseBody, AuthResponse.class);
        String token = "Bearer " + authResponse.getToken();

        mockMvc.perform(post("/api/support/chat")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"How do I pay fees?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists());
    }
}
