package com.chat_app.service.common;

import java.util.Map;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String text);
    

    void sendTemplateEmail(String to, String subject, String templateId, Map<String, Object> map);

    void sendVerifyEmail(String userFullname, String userEmail);
}
