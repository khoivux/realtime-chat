package com.chat_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    private String id;
    private String username;
    private String displayName;
    private String email;
    private String avatarUrl;
    private String role;
    private Boolean isBlocked;
    private Boolean isSocialLogin;
}
