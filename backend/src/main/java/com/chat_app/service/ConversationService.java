package com.chat_app.service;

import com.chat_app.dto.request.ConversationRequest;
import com.chat_app.dto.response.ConversationResponse;

import java.util.List;

public interface ConversationService {
    List<ConversationResponse> myConversations();

    ConversationResponse create(ConversationRequest request);
}
