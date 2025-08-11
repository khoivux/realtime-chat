package com.chat_app.constant;

public final class Constants {
    // Default Source
    public static final String DEFAULT_AVATAR_URL = "http://res.cloudinary.com/drdjvonsx/image/upload/v1741858825/ad2h5wifjk0xdqmawf9x.png";

    // Websocket
    public static final String TOPIC_CONVERSATIONS_PREFIX = "/topic/conversations/";
    public static final String TOPIC_CONVERSATION_UPDATE_PREFIX = "/topic/conversation-update/";

    // Redis Prefix
    public static final String ONLINE_PREFIX = "ONLINE:";

    private Constants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
}
