package com.chat_app.mapper;

import com.chat_app.dto.response.ConversationResponse;
import com.chat_app.model.Conversation;
import com.chat_app.model.User;
import com.chat_app.utils.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {
    public static ConversationResponse toResponse (Conversation conversation) {
        ConversationResponse response = ConversationResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .participantsHash(conversation.getParticipantHash())
                .participants(conversation.getParticipants())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
        User currUser = UserUtils.getCurrUser();
        conversation.getParticipants().stream()
                .filter(participantInfo -> !participantInfo.getUserId().equals(currUser.getId()))
                .findFirst().ifPresent(participantInfo -> {
                    response.setConvAvatar(participantInfo.getAvatarUrl());
                    response.setConvName(participantInfo.getDisplayName());
                });
        return response;
    }
}
