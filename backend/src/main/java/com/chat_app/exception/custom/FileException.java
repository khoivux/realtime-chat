package com.chat_app.exception.custom;

import com.chat_app.constant.ErrorCode;
import lombok.Getter;

@Getter
public class FileException extends RuntimeException{
    private final ErrorCode errorCode;
    public FileException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}