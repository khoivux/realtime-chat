package com.chat_app.service.user;

import com.chat_app.dto.request.UpdateUserRequest;
import com.chat_app.dto.response.UserResponse;
import com.chat_app.dto.response.stats.UserStatsResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.util.List;

public interface UserService {
    UserResponse getByUsername(String username);

    UserResponse updateProfile(UpdateUserRequest request);

    List<UserResponse> getList(String name);

    List<String> getOnlineUserIds();

    @PreAuthorize("hasRole('ADMIN')")
    void blockOrActive(String userId, Boolean blocked);

    UserStatsResponse getUserStats(LocalDate date);
}
