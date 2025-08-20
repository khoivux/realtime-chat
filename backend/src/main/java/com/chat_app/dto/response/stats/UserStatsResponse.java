package com.chat_app.dto.response.stats;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long onlineUsers;
    private long bannedUsers;
    private long dailyActiveUsers;
    private long newUsers;
}
