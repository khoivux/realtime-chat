package com.chat_app.security.oauth2.info;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public abstract class Oauth2UserInfo {
    protected Map<String, Object> attributes;

    public Oauth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getUsername();

    public abstract String getEmail();

    public abstract String getFullName();

    public abstract String getFirstname();

    public abstract String getLastname();

    public abstract String getAvatarUrl();
}
