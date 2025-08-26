package com.chat_app.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@Document(collection = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    @EqualsAndHashCode.Include
    private String username;
    private String firstname;
    private String lastname;
    private String password;
    private String displayName;
    private String email;
    private String avatarUrl;
    private Role role;
    @Builder.Default
    private Boolean isBlocked = false;
    @Builder.Default
    private Boolean isVerified = false;
    @Builder.Default
    private Boolean isSocialLogin = false;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
