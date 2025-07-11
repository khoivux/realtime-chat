package com.chat_app.service;

import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.request.ChatMessageRequest;
import com.chat_app.dto.response.ChatMessageResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.mapper.ChatMessageMapper;
import com.chat_app.model.ChatMessage;
import com.chat_app.model.ParticipantInfo;
import com.chat_app.model.User;
import com.chat_app.repository.ChatMessageRepository;
import com.chat_app.repository.ConversationRepository;
import com.chat_app.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService{
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageMapper chatMessageMapper;

    @Override
    public List<ChatMessageResponse> getMessagesByConversation(String conversationId) {
        String userId = UserUtils.getCurrUserId();
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND))
                .getParticipants()
                .stream()
                .filter(participantInfo -> userId.equals(participantInfo.getUserId()))
                .findAny()
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        List<ChatMessage> messages = chatMessageRepository.findByConversationId(conversationId);
        return messages.stream().map(this::toChatMessageResponse).toList();
    }

    @Override
    public ChatMessageResponse create(ChatMessageRequest request) {
        User currUser = UserUtils.getCurrUser();

        conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND))
                .getParticipants()
                .stream()
                .filter(participantInfo -> participantInfo.getUserId().equals(currUser.getId()))
                .findAny()
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        ChatMessage chatMessage = chatMessageMapper.toChatMessage(request);
        chatMessage.setSender(ParticipantInfo.builder()
                        .userId(currUser.getId())
                        .avatarUrl(currUser.getAvatarUrl())
                        .displayName(currUser.getDisplayName())
                .build());
        return toChatMessageResponse(chatMessageRepository.save(chatMessage));
    }

    @Override
    public void delete(String chatId) {
        ChatMessage chatMessage = chatMessageRepository.findByIdAndIsDeletedFalse(chatId)
                .orElseThrow(() -> new AppException(ErrorCode.CHATMESSAGE_NOT_FOUND));

        if(!chatMessage.getSender().getUserId().equals(UserUtils.getCurrUserId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        chatMessage.setDeleted(true);
        chatMessageRepository.save(chatMessage);
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage) {
        String currUserId = UserUtils.getCurrUserId();
        ChatMessageResponse response = chatMessageMapper.toChatMessageResponse(chatMessage);
        response.setMine(currUserId.equals(chatMessage.getSender().getUserId()));
        return response;
    }
}
