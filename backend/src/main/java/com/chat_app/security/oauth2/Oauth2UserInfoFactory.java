package com.chat_app.security.oauth2;


import com.chat_app.constant.Oauth2Provider;
import com.chat_app.security.oauth2.info.GithubUserInfo;
import com.chat_app.security.oauth2.info.GoogleUserInfo;
import com.chat_app.security.oauth2.info.Oauth2UserInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Oauth2UserInfoFactory {
    public static Oauth2UserInfo getOauth2UserInfo(Oauth2Provider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> new GoogleUserInfo(attributes);
            case GITHUB -> new GithubUserInfo(attributes);
        };
    }
}
