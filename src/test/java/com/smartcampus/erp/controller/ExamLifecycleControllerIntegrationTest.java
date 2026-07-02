package com.smartcampus.erp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.erp.BaseIntegrationTest;
import com.smartcampus.erp.dto.AuthResponse;
import com.smartcampus.erp.dto.RegisterRequest;
import com.smartcampus.erp.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ExamLifecycleControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void examLifecycle_workflow_integrationTest() throws Exception {
        // 1. Register student session
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Exam Candidate")
                .email("candidate.exam@aktu.edu")
                .password("candidateSecurePass")
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

        // Create student profile
        com.smartcampus.erp.dto.StudentProfileRequest profileRequest = com.smartcampus.erp.dto.StudentProfileRequest.builder()
                .rollNumber("AKTU-ROLL-999")
                .department("Computer Science")
                .semester(3)
                .dob(java.time.LocalDate.of(2004, 10, 15))
                .phone("+1234567890")
                .build();

        mockMvc.perform(put("/api/students/profile")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isOk());

        // 2. Submit Exam Form & Pay
        mockMvc.perform(post("/api/exams/form/submit")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"examId\":\"EXAM-AKTU-2026\",\"candidateName\":\"Exam Candidate\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.examId").value("EXAM-AKTU-2026"))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.paymentTransactionId").exists());

        // 3. Download Admit Card PDF stream
        mockMvc.perform(get("/api/exams/admit-card/download")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));

        // 4. Fetch Exam Results
        mockMvc.perform(get("/api/exams/results")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollNumber").exists())
                .andExpect(jsonPath("$.cgpa").exists());
    }
}
