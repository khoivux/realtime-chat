package com.chat_app.service;

import com.chat_app.dto.response.UserResponse;

public interface UserService {
    UserResponse getByUsername(String username);
}
