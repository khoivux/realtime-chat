package com.chat_app.controller;

import com.chat_app.dto.response.ApiResponse;
import com.chat_app.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j(topic = "NOTIFICATION-CONTROLLER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
@Tag(name = "Notification Controller")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/")
    public ApiResponse<?> getNotifications() {
        return ApiResponse.builder()
                .data(notificationService.getNotifications())
                .message("Lay danh sach thong bao thanh cong")
                .build();
    }

    @GetMapping("/count-unread")
    public ApiResponse<?> countUnread() {
        return ApiResponse.builder()
                .data(notificationService.countUnread())
                .build();
    }

    @PatchMapping("/mark-all-as-read")
    public ApiResponse<?> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.builder()
                .message("Danh dau thong bao da doc thanh cong")
                .build();
    }
}

