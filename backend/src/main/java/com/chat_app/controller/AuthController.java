package com.chat_app.controller;

import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.request.LoginRequest;
import com.chat_app.dto.request.LogoutRequest;
import com.chat_app.dto.request.RegisterRequest;
import com.chat_app.dto.response.ApiResponse;
import com.chat_app.dto.response.AuthResponse;
import com.chat_app.dto.response.UserResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.service.AuthService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth Controller")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Sign-up")
    @PostMapping("/register")
    public ApiResponse<UserResponse> signUp(@Valid @RequestBody RegisterRequest registerRequest) {
        return ApiResponse.<UserResponse>builder()
                .data(authService.register(registerRequest))
                .message("Đăng kí thành công")
                .build();
    }

    @Operation(summary = "Sign-in")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> signIn(@Valid @RequestBody LoginRequest loginRequest) {
        return ApiResponse.<AuthResponse>builder()
                .data(authService.login(loginRequest))
                .message("Đăng nhập thành công")
                .build();
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ApiResponse<?> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.builder()
                .message("Đăng xuất thành công")
                .build();
    }

    @Operation(summary = "Confirm Email")
    @PostMapping("/confirm-email")
    public ApiResponse<?> confirmEmail(@RequestParam String code) {
        return ApiResponse.builder()
                .message("Xác nhận thành công")
                .build();
    }
}
