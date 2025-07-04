package com.chat_app.constant;

public enum Oauth2Provider {
    GOOGLE("google"),
    GITHUB("github");

    private final String value;

    Oauth2Provider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static Oauth2Provider fromString(String value) {
        for (Oauth2Provider provider : Oauth2Provider.values()) {
            if (provider.value.equalsIgnoreCase(value)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Invalid OauthProvider: " + value);
    }
}
