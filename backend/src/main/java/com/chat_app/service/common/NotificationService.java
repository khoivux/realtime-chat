package com.chat_app.service.common;

import com.chat_app.model.ChatMessage;
import com.chat_app.model.Conversation;

public interface NotificationService {
    void sendToConversation(Conversation conversation, ChatMessage chatMessage);
}
