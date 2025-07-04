package com.chat_app.utils;

import com.chat_app.constant.ErrorCode;
import com.chat_app.exception.custom.AppException;
import com.chat_app.model.User;
import com.chat_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserUtils {
    private static UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        UserUtils.userRepository = userRepository;
    }

    public static User getCurrUser() {
        var context = SecurityContextHolder.getContext();
        return userRepository.findById(context.getAuthentication().getName())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public static String generateUsername(String email) {
        String base = email.split("@")[0];
        String username = base;
        while (userRepository.existsByUsername(username)) {
            username = base + "_" + UUID.randomUUID().toString().substring(0, 4);;
        }
        return username;
    }

}
