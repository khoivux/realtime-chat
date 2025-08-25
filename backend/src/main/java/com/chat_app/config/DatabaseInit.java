package com.chat_app.config;

import com.chat_app.constant.Constants;
import com.chat_app.constant.RoleName;
import com.chat_app.model.Role;
import com.chat_app.model.User;
import com.chat_app.repository.RoleRepository;
import com.chat_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseInit implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if(roleRepository.findAll().isEmpty()) {
            roleRepository.save(new Role(RoleName.ADMIN.toString()));
            roleRepository.save(new Role(RoleName.USER.toString()));
        }
        User admin = userRepository.findByUsername("admin").orElse(null);
        if(admin == null) {
            User newAdmin = User.builder()
                    .email("admin@gmail.com")
                    .username("admin")
                    .firstname("Admin")
                    .lastname(".")
                    .avatarUrl(Constants.DEFAULT_AVATAR_URL)
                    .role(roleRepository.findByName(RoleName.ADMIN))
                    .password(passwordEncoder.encode(Constants.DEFAULT_ADMIN_PASSWORD))
                    .build();
            userRepository.save(newAdmin);
        }
    }
}
