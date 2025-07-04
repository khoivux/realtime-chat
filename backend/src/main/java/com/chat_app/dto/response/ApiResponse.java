package com.chat_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"status_code", "data", "message"})
public class ApiResponse<T> implements Serializable {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String statusCode;
    private T data;
    private String message;
}