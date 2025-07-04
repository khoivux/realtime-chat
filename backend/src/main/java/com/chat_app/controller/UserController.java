package com.chat_app.controller;

import com.chat_app.dto.request.RegisterRequest;
import com.chat_app.dto.response.ApiResponse;
import com.chat_app.dto.response.UserResponse;
import com.chat_app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
}
