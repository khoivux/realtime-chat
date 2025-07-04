package com.chat_app.dto.request;

import com.chat_app.validator.annotation.PasswordMatches;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
@PasswordMatches
public class RegisterRequest {
    @NotBlank
    String firstname;
    @NotBlank
    String lastname;
    @Size(min = 3, message = "INVALID_USERNAME")
    String username;
    @Size(min = 5, message = "INVALID_PASSWORD")
    String password;
    String confirmPassword;
    @Email(message = "INVALID_EMAIL")
    String email;
}
