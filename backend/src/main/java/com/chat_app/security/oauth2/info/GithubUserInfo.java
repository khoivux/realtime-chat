package com.chat_app.security.oauth2.info;

import java.util.Map;

public class GithubUserInfo extends Oauth2UserInfo {
    public GithubUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getUsername() {
//        if (attributes.get("email") != null) {
//            return attributes.get("email").toString().split("@")[0];
//        }
        return attributes.get("login").toString();
    }

    @Override
    public String getEmail() {
        return attributes.get("email") != null ? attributes.get("email").toString() : "";
    }

    @Override
    public String getFullName() {
        return attributes.get("name") != null ? attributes.get("name").toString() : "";
    }

    @Override
    public String getFirstname() {
        String name = getFullName();
        return name.contains(" ") ? name.split(" ", 2)[0] : name;
    }

    @Override
    public String getLastname() {
        String name = getFullName();
        return name.contains(" ") ? name.split(" ", 2)[1] : "";
    }

    @Override
    public String getAvatarUrl() {
        return attributes.get("avatar_url") != null ? attributes.get("avatar_url").toString() : "";
    }
}
