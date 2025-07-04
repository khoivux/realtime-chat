package com.chat_app.service;

import com.chat_app.model.User;

public interface JwtService {
    String extractUserId(String token);
    String generateAccessToken(User user);
    boolean verifyToken(String token);
    String generateRefreshToken(User user);
}
