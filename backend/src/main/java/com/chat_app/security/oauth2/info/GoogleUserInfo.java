package com.chat_app.security.oauth2.info;

import java.util.Map;

public class GoogleUserInfo extends Oauth2UserInfo {
    public GoogleUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getUsername() {
        return attributes.get("email").toString().split("@")[0];
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getFullName() {
        return attributes.get("name").toString();
    }

    @Override
    public String getFirstname() {
        return attributes.get("given_name").toString();
    }

    @Override
    public String getLastname() {
        return attributes.get("family_name").toString();
    }

    @Override
    public String getAvatarUrl() {
        return attributes.get("picture").toString();
    }
}
