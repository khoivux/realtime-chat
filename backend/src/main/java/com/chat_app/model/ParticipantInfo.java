package com.chat_app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParticipantInfo {
    private String userId;
    private String displayName;
    private Instant joinedAt;
    private Instant updatedAt;
    private Instant leftAt;
    private String avatarUrl;
}
