package com.csims.controller;

import com.csims.dto.Dtos.*;
import com.csims.dto.ApiResponse;
import com.csims.security.CustomUserDetails;
import com.csims.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        UserDto userDto = authService.login(req, request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", userDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.ok("Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        UserDto userDto = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.ok(userDto));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest req,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails,
                                                            HttpServletRequest request) {
        authService.changePassword(userDetails.getId(), req, request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @PostMapping("/register-request")
    public ResponseEntity<ApiResponse<Void>> registerRequest(@Valid @RequestBody RegisterRequestDto req, HttpServletRequest request) {
        authService.registerRequest(req, request);
        return ResponseEntity.ok(ApiResponse.ok("Registration request submitted successfully. An administrator will review it.", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req, HttpServletRequest request) {
        authService.forgotPassword(req, request);
        return ResponseEntity.ok(ApiResponse.ok("If an account with that username or email exists, a reset request has been submitted to administrators.", null));
    }
}
