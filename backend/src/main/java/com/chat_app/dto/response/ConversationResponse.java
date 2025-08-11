package com.chat_app.dto.response;

import com.chat_app.constant.ChatType;
import com.chat_app.model.ParticipantInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    String id;
    ChatType type;
    String participantsHash;
    String name;
    String convAvatar;
    List<ParticipantInfo> participants;
    String lastMessagePreview;
    Long unreadCount;
    Instant lastActive;
    Instant createdAt;
    Instant updatedAt;
}
