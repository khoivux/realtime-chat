package com.chat_app.constant;

public final class Constants {
    // Auth
    public static final String JWT_SIGNATURE_ALGORITHM = "HmacSHA256";

    // Default Source
    public static final String DEFAULT_AVATAR_URL = "http://res.cloudinary.com/drdjvonsx/image/upload/v1741858825/ad2h5wifjk0xdqmawf9x.png";
    public static final String DEFAULT_ADMIN_PASSWORD = "12345";
    // Websocket
    public static final String TOPIC_CONVERSATIONS_PREFIX = "/topic/conversations/";

    // Redis Prefix
    public static final String ONLINE_PREFIX = "ONLINE:";

    private Constants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
}
