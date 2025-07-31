package com.chat_app.service;

import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.request.ChatMessageRequest;
import com.chat_app.dto.response.ChatMessageResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.mapper.ChatMessageMapper;
import com.chat_app.mapper.ParticipantInfoMapper;
import com.chat_app.model.ChatMessage;
import com.chat_app.model.ParticipantInfo;
import com.chat_app.model.User;
import com.chat_app.repository.ChatMessageRepository;
import com.chat_app.repository.ConversationRepository;
import com.chat_app.repository.UserRepository;
import com.chat_app.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService{
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final ParticipantInfoMapper participantInfoMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<ChatMessageResponse> getMessagesByConversation(String conversationId) {
        String userId = UserUtils.getCurrUserId();

        conversationService.checkConversationAccess(
                conversationRepository.findByIdAndIsDeletedFalse(conversationId)
                        .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND))
                , userId);

        List<ChatMessage> messages = chatMessageRepository.findByConversationId(conversationId);
        return messages.stream().map(this::toChatMessageResponse).toList();
    }

    @Override
    public void sendMessage(ChatMessageRequest request) {
        User sender = userRepository.findById(request.getSenderId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_EXISTED));

        conversationService.checkConversationAccess(
                conversationRepository.findByIdAndIsDeletedFalse(request.getConversationId())
                        .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND))
                , sender.getId());

        ChatMessage chatMessage = chatMessageMapper.toChatMessage(request);

        chatMessage.setSender(ParticipantInfo.builder()
                .userId(sender.getId())
                .displayName(sender.getDisplayName())
                .build());

        ChatMessageResponse response = toChatMessageResponse(chatMessageRepository.save(chatMessage));
        messagingTemplate.convertAndSend("/topic/conversations/" + request.getConversationId(), response);
    }

    @Override
    public ChatMessageResponse update(ChatMessageRequest request) {
        ChatMessage chatMessage = chatMessageRepository.findByIdAndIsDeletedFalse(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CHATMESSAGE_NOT_FOUND));

        if(!chatMessage.getSender().getUserId().equals(UserUtils.getCurrUserId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        chatMessage.setMessage(request.getMessage());
        ChatMessageResponse response = toChatMessageResponse(chatMessageRepository.save(chatMessage));
        messagingTemplate.convertAndSend("/topic/conversations/" + response.getConversationId(), response);
        return response;
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
        ChatMessageResponse response = toChatMessageResponse(chatMessageRepository.save(chatMessage));
        response.setDeleted(true);
        messagingTemplate.convertAndSend("/topic/conversations/" + response.getConversationId(), response);
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage) {
        ChatMessageResponse response = chatMessageMapper.toChatMessageResponse(chatMessage);

        if (chatMessage.getParentId() != null) {
            chatMessageRepository.findByIdAndIsDeletedFalse(chatMessage.getParentId())
                    .ifPresent(parent -> response.setParent(
                            ChatMessageResponse.builder()
                                    .id(parent.getId())
                                    .message(parent.getMessage())
                                    .build()
                    ));
        }
        response.setSender(participantInfoMapper.toResponse(chatMessage.getSender()));
        return response;
    }
}
