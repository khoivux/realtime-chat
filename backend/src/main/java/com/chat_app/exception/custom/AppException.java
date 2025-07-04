package com.chat_app.exception.custom;

import com.chat_app.constant.ErrorCode;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException{
    private final ErrorCode errorCode;
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}