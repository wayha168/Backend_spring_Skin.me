package com.project.skin_me.controller;

import com.project.skin_me.request.LoginRequest;
import com.project.skin_me.request.SignupRequest;
import com.project.skin_me.response.ApiResponse;
import com.project.skin_me.service.auth.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        return authService.login(loginRequest, request, response);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return authService.signup(signupRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        return authService.logout();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword) {
        return authService.resetPassword(email, password, confirmPassword);
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse> googleLogin(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request,
            HttpServletResponse response) {
        return authService.googleLogin(requestBody.get("code"), request, response);
    }
}