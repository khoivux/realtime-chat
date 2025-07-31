package com.chat_app.service;

import java.util.List;

import com.chat_app.dto.response.UserResponse;

public interface UserService {
    UserResponse getByUsername(String username);
    List<UserResponse> getList(String name);

    List<String> getOnlineUserIds();
}
