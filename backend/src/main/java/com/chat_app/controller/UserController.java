package com.chat_app.controller;

import com.chat_app.dto.request.UpdateUserRequest;
import com.chat_app.dto.response.ApiResponse;
import com.chat_app.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j(topic = "USER-CONTROLLER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get User")
    @GetMapping("/{username}")
    public ApiResponse<?> getByUsername(@PathVariable String username) {
        return ApiResponse.builder()
                .data(userService.getByUsername(username))
                .build();
    }

    @Operation(summary = "Get List")
    @GetMapping("/")
    public ApiResponse<?> getList(@RequestParam String name) {
        return ApiResponse.builder()
                .data(userService.getList(name))
                .build();
    }

    @Operation(summary = "Get online users")
    @GetMapping("/online")
    public ApiResponse<?> getOnlineUsers() {
        return ApiResponse.builder()
                .data(userService.getOnlineUserIds())
                .build();
    }

    @PatchMapping("/profile")
    public ApiResponse<?> updateProfile(@Valid @RequestBody  UpdateUserRequest request) {
        return ApiResponse.builder()
                .data(userService.updateProfile(request))
                .build();
    }
}
