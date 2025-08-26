package com.chat_app.service.auth;

import com.chat_app.constant.Constants;
import com.chat_app.constant.ErrorCode;
import com.chat_app.constant.RoleName;
import com.chat_app.dto.request.ChangePasswordRequest;
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
import com.chat_app.service.common.EmailService;
import com.chat_app.service.common.RedisTokenService;
import com.chat_app.utils.UserUtils;
import com.chat_app.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
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
        try {
            emailService.sendVerifyEmail(user, jwtService.generateAccessToken(user));
        } catch (Exception e) {

        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(user.getIsBlocked()) {
            throw new AppException(ErrorCode.BLOCKED_USER);
        }
        if(!user.getIsVerified()) {
            throw new AppException(ErrorCode.USER_IS_NOT_VERIFIED);
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

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = UserUtils.getCurrUser();
        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCHED);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public boolean verifyEmail(String token) {
        if (token == null || token.isBlank() || !jwtService.verifyToken(token)) {
            return false;
        }
        String userId = jwtService.extractUserId(token);
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();
        if (user.getIsVerified()) {
            return true;
        }
        user.setIsVerified(true);
        userRepository.save(user);
        return true;
    }
}
