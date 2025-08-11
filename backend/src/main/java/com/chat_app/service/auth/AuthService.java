package com.chat_app.service.auth;

import com.chat_app.dto.request.LoginRequest;
import com.chat_app.dto.request.LogoutRequest;
import com.chat_app.dto.request.RegisterRequest;
import com.chat_app.dto.response.AuthResponse;
import com.chat_app.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(LogoutRequest request);
}
