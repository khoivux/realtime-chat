package com.chat_app.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.Instant;

@Getter
@Setter
@Builder
@Document(collection = "notification")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notification {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    private String content;
    private String redirectUrl;
    @DocumentReference(lazy = true)
    private User receiver;
    @Builder.Default
    private boolean isRead = false;
    @CreatedDate
    private Instant createdAt;
    @Builder.Default
    private boolean isDeleted = false;
}
