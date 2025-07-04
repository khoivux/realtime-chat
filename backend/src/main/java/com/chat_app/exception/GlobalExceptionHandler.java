package com.chat_app.exception;

import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.response.ErrorResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.utils.DateTimeUtils;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse buildErrorResponse(ErrorCode errorCode, WebRequest request, String message) {
        return ErrorResponse.builder()
                .timestamp(DateTimeUtils.formatToVietnamTime(new Date()))
                .code(errorCode.getCode())
                .error(errorCode.getStatus().getReasonPhrase())
                .path(request.getDescription(false).replace("uri=", ""))
                .message(message != null ? message : errorCode.getMessage())
                .build();
    }

    private String mapAttributes(String message, Map<String, Object> attributes) {
        try {
            String minValue = attributes.get("min").toString();
            return message.replace("{min}", minValue);
        } catch (Exception e) {
            return null;
        }
    }

    @ExceptionHandler({AppException.class})
    public ResponseEntity<ErrorResponse> handleAppException(AppException exception, WebRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        ErrorResponse errorResponse = buildErrorResponse(errorCode, request, null);
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorResponse);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exception, WebRequest request) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        ErrorResponse errorResponse = buildErrorResponse(errorCode, request, null);
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorResponse);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception, WebRequest request) {
        ErrorCode errorCode = ErrorCode.valueOf(Objects.requireNonNull(exception.getFieldError()).getDefaultMessage());

        var constraintViolation = exception.getBindingResult()
                .getAllErrors().getFirst().unwrap(ConstraintViolation.class);

        Map<String, Object> attributes = constraintViolation.getConstraintDescriptor().getAttributes();
        String message = mapAttributes(errorCode.getMessage(), attributes);

        ErrorResponse errorResponse = buildErrorResponse(errorCode, request, Objects.nonNull(message)
                ? message : errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorResponse);
    }
}