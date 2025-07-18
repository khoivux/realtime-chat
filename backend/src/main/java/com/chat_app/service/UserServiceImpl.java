package com.chat_app.service;

import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.response.UserResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.mapper.UserMapper;
import com.chat_app.model.User;
import com.chat_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
}
