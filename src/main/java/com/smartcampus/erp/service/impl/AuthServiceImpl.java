package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.dto.*;
import com.smartcampus.erp.entity.User;
import com.smartcampus.erp.exception.BadRequestException;
import com.smartcampus.erp.exception.ResourceNotFoundException;
import com.smartcampus.erp.exception.ValidationException;
import com.smartcampus.erp.repository.UserRepository;
import com.smartcampus.erp.security.JwtUtil;
import com.smartcampus.erp.security.TokenBlacklistService;
import com.smartcampus.erp.security.UserPrincipal;
import com.smartcampus.erp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService blacklistService;
    private final RedisTemplate<String, String> redisTemplate;

    private final Map<String, String> inMemoryResetTokens = new ConcurrentHashMap<>();
    private final Map<String, Long> inMemoryResetExpiry = new ConcurrentHashMap<>();

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager,
                           TokenBlacklistService blacklistService,
                           @Autowired(required = false) RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.blacklistService = blacklistService;
        this.redisTemplate = redisTemplate;
    }

    private final Map<String, String> inMemoryRefreshTokens = new ConcurrentHashMap<>();

    private String generateAndPersistRefreshToken(String email) {
        String token = java.util.UUID.randomUUID().toString();
        long ttlDays = 7;
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set("refresh_token:" + token, email, ttlDays, TimeUnit.DAYS);
            } catch (Exception e) {
                inMemoryRefreshTokens.put(token, email);
            }
        } else {
            inMemoryRefreshTokens.put(token, email);
        }
        return token;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already taken");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        UserPrincipal principal = new UserPrincipal(savedUser);
        String token = jwtUtil.generateToken(principal);
        String refreshToken = generateAndPersistRefreshToken(savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtUtil.generateToken(principal);
        String refreshToken = generateAndPersistRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        String tokenKey = "refresh_token:" + token;
        String email = null;

        if (redisTemplate != null) {
            try {
                email = redisTemplate.opsForValue().get(tokenKey);
            } catch (Exception e) {
                // fallback
            }
        }

        if (email == null) {
            email = inMemoryRefreshTokens.get(token);
        }

        if (email == null) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        // Invalidate old refresh token (single-use rotation)
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(tokenKey);
            } catch (Exception e) {
                // ignore
            }
        }
        inMemoryRefreshTokens.remove(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User associated with token not found"));

        UserPrincipal principal = new UserPrincipal(user);
        String newAccessToken = jwtUtil.generateToken(principal);
        String newRefreshToken = generateAndPersistRefreshToken(email);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                String email = jwtUtil.extractUsername(jwt);
                Date expiration = jwtUtil.extractExpiration(jwt);
                long ttl = expiration.getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    blacklistService.blacklistToken(jwt, ttl);
                }

                // Invalidate refresh tokens associated with this user email
                if (email != null) {
                    if (redisTemplate != null) {
                        try {
                            java.util.Set<String> keys = redisTemplate.keys("refresh_token:*");
                            if (keys != null) {
                                for (String key : keys) {
                                    String cachedEmail = redisTemplate.opsForValue().get(key);
                                    if (email.equals(cachedEmail)) {
                                        redisTemplate.delete(key);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                    inMemoryRefreshTokens.entrySet().removeIf(entry -> email.equals(entry.getValue()));
                }
            } catch (Exception e) {
                // Token might be malformed or already expired; ignore
            }
        }
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + request.getEmail()));

        String token = java.util.UUID.randomUUID().toString();
        long ttlMinutes = 15;

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set("reset_token:" + token, user.getEmail(), ttlMinutes, TimeUnit.MINUTES);
            } catch (Exception e) {
                // fallback to in-memory if Redis write fails
                inMemoryResetTokens.put(token, user.getEmail());
                inMemoryResetExpiry.put(token, System.currentTimeMillis() + (ttlMinutes * 60 * 1000));
            }
        } else {
            inMemoryResetTokens.put(token, user.getEmail());
            inMemoryResetExpiry.put(token, System.currentTimeMillis() + (ttlMinutes * 60 * 1000));
        }

        System.out.println("----------------------------------------");
        System.out.println("PASSWORD RESET LINK FOR: " + user.getEmail());
        System.out.println("http://localhost:8080/reset-password#token=" + token);
        System.out.println("----------------------------------------");
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = null;
        String tokenKey = "reset_token:" + request.getToken();

        if (redisTemplate != null) {
            try {
                email = redisTemplate.opsForValue().get(tokenKey);
            } catch (Exception e) {
                // fallback
            }
        }

        if (email == null) {
            String cachedEmail = inMemoryResetTokens.get(request.getToken());
            Long expiry = inMemoryResetExpiry.get(request.getToken());
            if (cachedEmail != null && expiry != null && expiry > System.currentTimeMillis()) {
                email = cachedEmail;
            }
        }

        if (email == null) {
            throw new BadRequestException("Invalid or expired password reset token");
        }

        Map<String, String> errors = new HashMap<>();
        if (request.getNewPassword().length() < 8) {
            errors.put("password", "Too weak");
        } else {
            boolean hasUpper = false;
            boolean hasDigit = false;
            boolean hasSpecial = false;
            for (char c : request.getNewPassword().toCharArray()) {
                if (Character.isUpperCase(c)) hasUpper = true;
                else if (Character.isDigit(c)) hasDigit = true;
                else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
            }
            if (!hasUpper || !hasDigit || !hasSpecial) {
                errors.put("password", "Too weak");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        if (redisTemplate != null) {
            try {
                redisTemplate.delete(tokenKey);
            } catch (Exception e) {
                // ignore
            }
        }
        inMemoryResetTokens.remove(request.getToken());
        inMemoryResetExpiry.remove(request.getToken());
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect current password");
        }

        Map<String, String> errors = new HashMap<>();
        if (request.getNewPassword().length() < 8) {
            errors.put("password", "Too weak");
        } else {
            boolean hasUpper = false;
            boolean hasDigit = false;
            boolean hasSpecial = false;
            for (char c : request.getNewPassword().toCharArray()) {
                if (Character.isUpperCase(c)) hasUpper = true;
                else if (Character.isDigit(c)) hasDigit = true;
                else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
            }
            if (!hasUpper || !hasDigit || !hasSpecial) {
                errors.put("password", "Too weak");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
