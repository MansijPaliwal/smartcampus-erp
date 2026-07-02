package com.smartcampus.erp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.erp.BaseIntegrationTest;
import com.smartcampus.erp.dto.LoginRequest;
import com.smartcampus.erp.dto.RegisterRequest;
import com.smartcampus.erp.entity.Role;
import com.smartcampus.erp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerIntegrationTest extends BaseIntegrationTest {

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
    void registerAndLogin_integrationTest() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Bob")
                .email("bob@test.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();

        // 1. Register Bob
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("bob@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // 2. Login Bob
        LoginRequest loginRequest = LoginRequest.builder()
                .email("bob@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("bob@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
