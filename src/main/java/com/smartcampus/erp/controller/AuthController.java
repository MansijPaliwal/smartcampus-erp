package com.smartcampus.erp.controller;

import com.smartcampus.erp.dto.*;
import com.smartcampus.erp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication and session management APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account (STUDENT, FACULTY, or ADMIN) and retrieve a JWT access token.")
    @ApiResponse(responseCode = "200", description = "Successfully registered user and retrieved JWT token")
    @ApiResponse(responseCode = "400", description = "Email is already taken or invalid register payload")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and generate JWT token", description = "Authenticate using email and password to receive a JWT access token.")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated user and retrieved JWT token")
    @ApiResponse(responseCode = "400", description = "Invalid email/password combination or user not found")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Blacklist the JWT access token in Redis to invalidate the session.")
    @ApiResponse(responseCode = "204", description = "Successfully logged out user and blacklisted token")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Accept email, generate a token, store in Redis for 15 minutes, and output the reset link to console.")
    @ApiResponse(responseCode = "200", description = "Successfully registered forgot password token")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token", description = "Accept token + new password, validate token from Redis, and update password.")
    @ApiResponse(responseCode = "200", description = "Successfully updated password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change own password", description = "Authenticated user changes their own password by verifying current password.")
    @ApiResponse(responseCode = "200", description = "Successfully changed password")
    public ResponseEntity<Void> changePassword(Principal principal, @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.ok().build();
    }
}
