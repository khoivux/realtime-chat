package com.chat_app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParticipantInfo {
    private String userId;
    private String displayName;
    private String avatarUrl;

    private boolean isAdmin;

    private Instant joinedAt;
    private Instant updatedAt;
}
