package com.chat_app.model;

import com.chat_app.constant.MessageType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Document(collection = "chat_message")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChatMessage {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    @Indexed
    private String conversationId;
    private String message;
    private ParticipantInfo sender;
    private String parentId;
    private MessageType type;
    private String mediaUrl;
    @CreatedDate
    Instant createdAt;
    @LastModifiedDate
    Instant updatedAt;
    @Builder.Default
    boolean isDeleted = false;

    public ChatMessage() {
        // constructor mặc định public
    }
}
