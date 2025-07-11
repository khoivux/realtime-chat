package com.chat_app.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParticipantInfo {
    private String userId;
    private String displayName;
    private String avatarUrl;

    private boolean isAdmin;

    private Instant joinedAt;
    private Instant updatedAt;
}
