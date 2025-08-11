package com.chat_app.service.chat;

import com.chat_app.dto.request.ChatMessageRequest;
import com.chat_app.dto.response.ChatMessageResponse;
import com.chat_app.dto.response.OffsetResponse;

public interface ChatMessageService {
    OffsetResponse<ChatMessageResponse> getMessagesByConversation(String conversationId, int offset, int limit);

    void sendMessage(ChatMessageRequest request);

    ChatMessageResponse update(ChatMessageRequest request);

    void delete(String chatId);

}
