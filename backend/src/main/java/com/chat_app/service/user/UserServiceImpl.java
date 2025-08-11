package com.chat_app.service.user;

import com.chat_app.constant.Constants;
import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.request.UpdateUserRequest;
import com.chat_app.dto.response.UserResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.mapper.UserMapper;
import com.chat_app.model.User;
import com.chat_app.repository.UserRepository;
import com.chat_app.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public UserResponse getByUsername(String username) {

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateProfile(UpdateUserRequest request) {
        User user = UserUtils.getCurrUser();
        if(userRepository.existsByUsername(request.getUsername())
                && !user.getUsername().equals(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        user.setAvatarUrl(request.getAvatarUrl());
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getList(String name) {
        log.info("SEARCH USER BY: {}", name);
        List<User> users = userRepository.findByUsernameOrDisplayName(name);
        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<String> getOnlineUserIds() {
        Set<String> keys = redisTemplate.keys(Constants.ONLINE_PREFIX + "*");
        if (keys == null) return List.of();
        return keys.stream()
                .map(key -> key.replace(Constants.ONLINE_PREFIX , ""))
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void blockOrActive(String userId, Boolean blocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setIsBlocked(blocked);
        userRepository.save(user);
    }
}
