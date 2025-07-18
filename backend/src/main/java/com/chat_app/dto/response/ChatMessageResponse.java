package com.chat_app.dto.response;

import com.chat_app.model.ParticipantInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    String id;
    String conversationId;
    String message;
    ParticipantInfo sender;
    Instant createdAt;
    Instant updatedAt;
    ChatMessageResponse parent;
    boolean isDeleted;
    boolean isMine;
}
