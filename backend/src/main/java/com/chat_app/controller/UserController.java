package com.chat_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chat_app.dto.response.ApiResponse;
import com.chat_app.dto.response.UserResponse;
import com.chat_app.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get By Username")
    @GetMapping("/{username}")
    public ApiResponse<UserResponse> getByUsername(@PathVariable String username) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getByUsername(username))
                .message("Lấy thành công thông tin user @" + username)
                .build();
    }

    @Operation(summary = "Get List")
    @GetMapping("/")
    public ApiResponse<?> getList(@RequestParam String name) {
        return ApiResponse.builder()
                .data(userService.getList(name))
                .message("Lấy thành công danh sách user")
                .build();
    }
}
