package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.AuthResponse;
import com.smartcampus.erp.dto.LoginRequest;
import com.smartcampus.erp.dto.RegisterRequest;
import com.smartcampus.erp.entity.Role;
import com.smartcampus.erp.entity.User;
import com.smartcampus.erp.exception.BadRequestException;
import com.smartcampus.erp.repository.UserRepository;
import com.smartcampus.erp.security.JwtUtil;
import com.smartcampus.erp.security.TokenBlacklistService;
import com.smartcampus.erp.security.UserPrincipal;
import com.smartcampus.erp.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenBlacklistService blacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Alice")
                .email("alice@test.com")
                .password("password")
                .role(Role.STUDENT)
                .build();

        loginRequest = LoginRequest.builder()
                .email("alice@test.com")
                .password("password")
                .build();

        user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@test.com")
                .password("hashed_password")
                .role(Role.STUDENT)
                .enabled(true)
                .build();
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(any(UserPrincipal.class))).thenReturn("dummy_jwt");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("dummy_jwt", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("alice@test.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_emailTaken_throwsBadRequest() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(UserPrincipal.class))).thenReturn("dummy_jwt");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("dummy_jwt", response.getToken());
        assertEquals(1L, response.getUserId());
    }

    @Test
    void logout_success() {
        String token = "Bearer dummy_token";
        Date expDate = new Date(System.currentTimeMillis() + 10000);
        when(jwtUtil.extractExpiration(any())).thenReturn(expDate);

        authService.logout(token);

        verify(blacklistService, times(1)).blacklistToken(eq("dummy_token"), anyLong());
    }
}
