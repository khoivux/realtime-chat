package com.chat_app.controller;

import com.chat_app.service.common.FCMTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j(topic = "FCM_CONTROLLER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcm")
@Tag(name = "FCM Controller")
public class FCMController {
    private final FCMTokenService fcmTokenService;

    @PostMapping("/save")
    public void saveToken(@RequestParam String userId, @RequestParam String token) {
        fcmTokenService.saveToken(userId, token);
    }

    @GetMapping("/token/{userId}")
    public String getToken(@PathVariable String userId) {
        return fcmTokenService.getToken(userId);
    }

    @DeleteMapping("/delete/{userId}")
    public void deleteToken(@PathVariable String userId) {
        fcmTokenService.deleteToken(userId);
    }
}
