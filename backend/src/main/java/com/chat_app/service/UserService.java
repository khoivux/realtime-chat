package com.chat_app.service;

import com.chat_app.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getByUsername(String username);
    List<UserResponse> getList(String name);
}
