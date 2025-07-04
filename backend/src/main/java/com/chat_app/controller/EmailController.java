package com.chat_app.controller;

import com.chat_app.service.EmailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j(topic = "EMAIL-CONTROLLER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
@Tag(name = "Email Controller")
public class EmailController {
    private final EmailService emailService;

    @GetMapping("/send-email")
    public void send(@RequestParam  String to, String subject, String content) {
        emailService.send(to, subject, content);
    }

    @GetMapping("/verify-email")
    public void sendVerificationEmail(@RequestParam  String to, String name) {
        emailService.sendVerificationEmail(to, name);
    }
}
