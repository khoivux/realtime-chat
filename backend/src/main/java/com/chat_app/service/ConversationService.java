package com.chat_app.service;

import com.chat_app.dto.request.ConversationRequest;
import com.chat_app.dto.request.ParticipantRequest;
import com.chat_app.dto.request.UpdateConversationRequest;
import com.chat_app.dto.response.ConversationResponse;
import com.chat_app.model.Conversation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ConversationService {
    List<ConversationResponse> myConversations();

    ConversationResponse getConversation(String conversationId);

    ConversationResponse create(ConversationRequest request);

    ConversationResponse update(UpdateConversationRequest request);


    @Transactional
    void addParticipant(ParticipantRequest request);

    @Transactional
    void removeParticipant(ParticipantRequest request);

    void checkConversationAccess(Conversation conversation, String userId);
}
