package com.chat_app.validator;

import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.request.RegisterRequest;
import com.chat_app.exception.custom.AppException;
import com.chat_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserValidator {
    private static UserRepository userRepository;

    @Autowired
    public UserValidator(UserRepository userRepository) {
        UserValidator.userRepository = userRepository; // Inject repository vào static field
    }

    public static void validateUserAvailability(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
    }
}