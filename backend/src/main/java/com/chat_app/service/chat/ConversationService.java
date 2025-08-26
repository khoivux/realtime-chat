package com.chat_app.service.chat;

import com.chat_app.dto.request.ConversationRequest;
import com.chat_app.dto.request.ParticipantRequest;
import com.chat_app.dto.request.UpdateConversationRequest;
import com.chat_app.dto.response.ConversationResponse;
import com.chat_app.model.Conversation;

import java.util.List;

public interface ConversationService {
    List<ConversationResponse> myConversations();
    ConversationResponse getConversation(String conversationId);
    ConversationResponse create(ConversationRequest request);
    ConversationResponse update(UpdateConversationRequest request);
    ConversationResponse updateAvatar(UpdateConversationRequest request);
    void addParticipant(ParticipantRequest request);
    void removeParticipant(ParticipantRequest request);
    void markAsRead(String conversationId);
    void checkConversationAccess(Conversation conversation, String userId);
}
