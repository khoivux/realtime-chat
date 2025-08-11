package com.chat_app.service.auth;

import com.chat_app.constant.Constants;
import com.chat_app.service.common.RedisTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.chat_app.constant.ErrorCode;
import com.chat_app.constant.RoleName;
import com.chat_app.dto.request.LoginRequest;
import com.chat_app.dto.request.LogoutRequest;
import com.chat_app.dto.request.RegisterRequest;
import com.chat_app.dto.response.AuthResponse;
import com.chat_app.dto.response.UserResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.exception.custom.InvalidDataException;
import com.chat_app.mapper.UserMapper;
import com.chat_app.model.RedisToken;
import com.chat_app.model.Role;
import com.chat_app.model.User;
import com.chat_app.repository.RoleRepository;
import com.chat_app.repository.UserRepository;
import com.chat_app.utils.UserUtils;
import com.chat_app.validator.UserValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserResponse register(RegisterRequest request) {
        UserValidator.validateUserAvailability(request);
        Role role = roleRepository.findByName(RoleName.USER);

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .avatarUrl(Constants.DEFAULT_AVATAR_URL)
                .displayName(request.getFirstname() + " " + request.getLastname())
                .role(role)
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(user.getIsBlocked()) {
            throw new AppException(ErrorCode.BLOCKED_USER);
        }
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        redisTokenService.save(RedisToken.builder().id(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(LogoutRequest request) {
        String token = request.getToken();
        if(token.isEmpty() || !jwtService.verifyToken(token)) {
            throw  new InvalidDataException("Token is not valid");
        }

        String currentUsername = UserUtils.getCurrUser().getUsername();
        redisTokenService.delete(currentUsername);
    }
}
