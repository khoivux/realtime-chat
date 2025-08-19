package com.chat_app.service.common;

import com.chat_app.model.FCMToken;
import com.chat_app.repository.FCMTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FCMTokenService {
    private final FCMTokenRepository fcmTokenRepository;

    public void saveToken(String userId, String token) {
        FCMToken fcmToken = fcmTokenRepository.findById(userId).orElse(new FCMToken());
        fcmToken.setToken(token);
        fcmToken.setUserId(userId);
        fcmTokenRepository.save(fcmToken);
    }

    public String getToken(String userId) {
        return fcmTokenRepository.findById(userId)
                .map(FCMToken::getToken)
                .orElse(null);
    }

    public void deleteToken(String userId) {
        fcmTokenRepository.deleteById(userId);
    }
}
