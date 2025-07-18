package com.chat_app.service;

import com.chat_app.dto.request.ConversationRequest;
import com.chat_app.dto.request.ParticipantRequest;
import com.chat_app.dto.request.UpdateConversationRequest;
import com.chat_app.dto.response.ConversationResponse;

import java.util.List;

public interface ConversationService {
    List<ConversationResponse> myConversations();

    ConversationResponse getConversation(String conversationId);

    ConversationResponse create(ConversationRequest request);

    ConversationResponse update(UpdateConversationRequest request);

    void addOrDeleteUser(ParticipantRequest request);
}
