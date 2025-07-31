package com.chat_app.service;

import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.response.UserResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.mapper.UserMapper;
import com.chat_app.model.User;
import com.chat_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private static final String ONLINE_PREFIX = "ONLINE:";
    private final StringRedisTemplate redisTemplate;

    @Override
    public UserResponse getByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getList(String name) {
        List<User> users = userRepository.findByUsernameOrDisplayName(name);
        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<String> getOnlineUserIds() {
        Set<String> keys = redisTemplate.keys(ONLINE_PREFIX + "*");
        if (keys == null) return List.of();

        return keys.stream()
                .map(key -> key.replace(ONLINE_PREFIX, ""))
                .toList();
    }
}
