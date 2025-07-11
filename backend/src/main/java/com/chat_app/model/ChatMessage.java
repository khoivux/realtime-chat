package com.chat_app.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
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
    @CreatedDate
    Instant createdAt;
    @LastModifiedDate
    Instant updatedAt;
    @Builder.Default
    boolean isDeleted = false;
}
