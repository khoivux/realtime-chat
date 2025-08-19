package com.chat_app.service.common;

import com.chat_app.exception.custom.ResourceNotFoundException;
import com.chat_app.model.RedisToken;
import com.chat_app.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisTokenService {
    private final RedisTokenRepository redisTokenRepository;

    public void save(RedisToken token) {
        redisTokenRepository.save(token);
    }

    public void delete(String id) {
        redisTokenRepository.deleteById(id);
    }

    public RedisToken getById(String id) {
        return redisTokenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Redis Token not found"));
    }
}