package com.chat_app.controller;

import com.chat_app.dto.response.ApiResponse;
import com.chat_app.dto.response.stats.ChatStatsResponse;
import com.chat_app.dto.response.stats.UserStatsResponse;
import com.chat_app.service.chat.ChatMessageService;
import com.chat_app.service.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Controller")
public class AdminController {
    private final UserService userService;
    private final ChatMessageService chatMessageService;

    @GetMapping("/stats/chat")
    public ApiResponse<?> getDailyChatStats(@RequestParam(required = false)
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        ChatStatsResponse stats = chatMessageService.getStats(targetDate);
        return ApiResponse.builder()
                .data(stats)
                .build();
    }

    @GetMapping("/stats/user")
    public ApiResponse<?> getDailyUserStats(@RequestParam(required = false)
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        UserStatsResponse stats = userService.getUserStats(targetDate);
        return ApiResponse.builder()
                .data(stats)
                .build();
    }

    @PatchMapping("/block-user/{userId}")
    public void blockOrActiveUser(@Valid @PathVariable String userId,
                                  @RequestParam boolean blocked) {
        userService.blockOrActive(userId, blocked);
    }
}
