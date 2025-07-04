package com.chat_app.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @Size(min = 3, message = "INVALID_USERNAME")
    String username;
    @Size(min = 5, message = "INVALID_PASSWORD")
    String password;
}
