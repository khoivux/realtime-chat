package com.chat_app.service.common;

import com.chat_app.model.User;

import java.util.Map;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String text);
    void sendTemplateEmail(String to, String subject, String templateId, Map<String, Object> map);
    void sendVerifyEmail(User user, String token);
}
