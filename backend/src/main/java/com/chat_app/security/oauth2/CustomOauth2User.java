package com.chat_app.security.oauth2;

import com.chat_app.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CustomOauth2User implements OAuth2User {

    private final String id;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    public CustomOauth2User(User user, Map<String, Object> attributes) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}