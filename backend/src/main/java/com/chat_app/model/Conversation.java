package com.chat_app.model;

import com.chat_app.constant.ChatType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@Document(collection = "conversation")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Conversation {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    private ChatType type;
    private String name;

    @Indexed(unique = true)
    String participantHash;
    List<ParticipantInfo> participants;
    private String createdBy;
    @Builder.Default
    private Boolean isDeleted = false;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
    @Builder.Default
    private Instant deleteAt = null;
}
