package com.chat_app.service;

import com.chat_app.dto.request.ChatMessageRequest;
import com.chat_app.dto.response.ChatMessageResponse;

import java.util.List;

public interface ChatMessageService {
    List<ChatMessageResponse> getMessagesByConversation(String conversationId);

    ChatMessageResponse create(ChatMessageRequest request);

    void delete(String chatId);
}
