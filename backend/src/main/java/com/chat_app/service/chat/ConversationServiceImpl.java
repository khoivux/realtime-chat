package com.chat_app.service.chat;

import com.chat_app.constant.ChatType;
import com.chat_app.constant.Constants;
import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.request.ConversationRequest;
import com.chat_app.dto.request.ParticipantRequest;
import com.chat_app.dto.request.UpdateConversationRequest;
import com.chat_app.dto.response.ConversationResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.mapper.ParticipantInfoMapper;
import com.chat_app.model.ChatMessage;
import com.chat_app.model.Conversation;
import com.chat_app.model.ParticipantInfo;
import com.chat_app.repository.ChatMessageRepository;
import com.chat_app.repository.ConversationRepository;
import com.chat_app.repository.UserRepository;
import com.chat_app.utils.MessageUtils;
import com.chat_app.utils.ParticipantUtils;
import com.chat_app.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService{
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ParticipantInfoMapper participantInfoMapper;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Returns all active conversations that the current user is participating in.
     *
     * @return list of ConversationResponse
     */
    @Override
    public List<ConversationResponse> myConversations() {
        String currUserId = UserUtils.getCurrUserId();
        List<Conversation> conversations = conversationRepository.findAllActiveConversationsByUserId(currUserId);
        return conversations.stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(ConversationResponse::getLastActive,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
    /**
     * Get a conversation by id
     * @param conversationId conversation's id
     * @return Conversation Response
     */
    @Override
    public ConversationResponse getConversation(String conversationId) {
        Conversation conversation = conversationRepository.findByIdAndIsDeletedFalse(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        return toResponse(conversation);
    }

    /**
     * Create a new conversation
     * @param request conversation creation request
     * @return Conversation Response
     */
    @Override
    @Transactional
    public ConversationResponse create(ConversationRequest request) {
        String creatorId = UserUtils.getCurrUserId();

        List<String> userIds = new ArrayList<>(request.getParticipantIds());
        if (!userIds.contains(creatorId)) {
            userIds.add(creatorId);
        }

        List<String> sortedIds = userIds.stream().distinct().sorted().toList();

        String userIdHash = ParticipantUtils.generateParticipantHash(sortedIds);
        Conversation conversation = conversationRepository.findByParticipantHash(userIdHash)
                .orElseGet(() -> {
                    // Create participant list
                    List<ParticipantInfo> participants = createParticipants(sortedIds);
                    if (participants.isEmpty()) {
                        throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                    }
                    Conversation newConversation = Conversation.builder()
                            .type(request.getType())
                            .createdBy(creatorId)
                            .participantHash(userIdHash)
                            .participants(participants)
                            .build();
                    if(request.getType() == ChatType.GROUP) {
                        newConversation.setName(participants.getFirst().getDisplayName()
                                + ", "
                                + participants.getLast().getDisplayName());
                    }
                    return conversationRepository.save(newConversation);
                });

        ConversationResponse response = toResponse(conversation);

        conversation.getParticipants().stream()
                .map(ParticipantInfo::getUserId)
                .forEach(userId -> messagingTemplate.convertAndSend(Constants.TOPIC_CONVERSATIONS_PREFIX + userId, response));

        return response;
    }

    /**
     * Updates group conversation information (e.g., name).
     *
     * @param request update request
     * @return updated ConversationResponse
     */
    @Override
    public ConversationResponse update(UpdateConversationRequest request) {
        Conversation conversation = conversationRepository.findByIdAndIsDeletedFalse(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        checkConversationAccess(conversation, UserUtils.getCurrUserId());
        if(conversation.getType() == ChatType.DIRECT) {
            throw new AppException(ErrorCode.CONVERSATION_NOT_FOUND);
        }
        if(request.getName() != null) {
            conversation.setName(request.getName());
        }

        Conversation savedConversation = conversationRepository.save(conversation);
        ConversationResponse response = toResponse(savedConversation);
        messagingTemplate.convertAndSend(Constants.TOPIC_CONVERSATION_UPDATE_PREFIX + conversation.getId(), response);
        return toResponse(conversationRepository.save(conversation));
    }

    @Override
    public ConversationResponse updateAvatar(UpdateConversationRequest request) {
        Conversation conversation = conversationRepository.findByIdAndIsDeletedFalse(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        checkConversationAccess(conversation, UserUtils.getCurrUserId());
        if(conversation.getType() == ChatType.DIRECT) {
            throw new AppException(ErrorCode.CONVERSATION_NOT_FOUND);
        }

        if(request.getAvatarUrl() != null) {
            conversation.setAvatarUrl(request.getAvatarUrl());
        }

        Conversation savedConversation = conversationRepository.save(conversation);
        ConversationResponse response = toResponse(savedConversation);

        messagingTemplate.convertAndSend(Constants.TOPIC_CONVERSATION_UPDATE_PREFIX + conversation.getId(), response);
        return response;
    }

    @Override
    @Transactional
    public void addParticipant(ParticipantRequest request) {
        Conversation conversation = getValidGroupConversation(request.getConversationId());
        List<ParticipantInfo> participants = conversation.getParticipants();

        ParticipantInfo participant = participants.stream()
                .filter(p -> p.getUserId().equals(request.getUserId()))
                .findAny()
                .orElseGet(() -> {
                    ParticipantInfo newParticipant = ParticipantInfo.builder()
                            .userId(request.getUserId())
                            .build();
                    participants.add(newParticipant);
                    return newParticipant;
                });

        participant.setJoinedAt(Instant.now());
        participant.setLeftAt(null);
        conversationRepository.save(conversation);
    }

    @Override
    @Transactional
    public void removeParticipant(ParticipantRequest request) {
        String currUserId = UserUtils.getCurrUserId();
        Conversation conversation = getValidGroupConversation(request.getConversationId());

        if(!conversation.getCreatedBy().equals(currUserId) && !currUserId.equals(request.getUserId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        ParticipantInfo participant = conversation.getParticipants().stream()
                .filter(p -> p.getUserId().equals(request.getUserId()))
                .findAny()
                .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED));

        participant.setLeftAt(Instant.now());
        conversationRepository.save(conversation);
    }

    @Override
    public void markAsRead(String conversationId) {
        String userId = UserUtils.getCurrUserId();

        Conversation conversation = conversationRepository.findByIdAndIsDeletedFalse(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        ParticipantInfo participant = conversation.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId)).findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PARTICIPANT_NOT_EXISTED));

        ChatMessage lastMessage = chatMessageRepository
                .findFirstByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(conversation.getId())
                .orElse(null);
        String lastSeenMessageId = lastMessage != null ? lastMessage.getId() : null;
        participant.setLastSeenMessage(lastSeenMessageId);
        conversationRepository.save(conversation);
    }


    @Override
    public void checkConversationAccess(Conversation conversation, String userId) {
        conversation.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId) && p.getLeftAt() == null)
                .findAny()
                .orElseThrow(() -> new AppException(ErrorCode.PARTICIPANT_NOT_EXISTED));
    }

    private Long getUnreadCount(Conversation conversation, String userId) {
        ParticipantInfo participant = conversation.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId)).findFirst().orElseThrow();

        Instant lastReadAt = Instant.EPOCH;
        if (participant.getLastSeenMessage() != null) {
            ChatMessage msg = chatMessageRepository.findByIdAndIsDeletedFalse(participant.getLastSeenMessage())
                    .orElse(null);
            if (msg != null) lastReadAt = msg.getCreatedAt();
        }
        return chatMessageRepository.countByConversationIdAndCreatedAtAfterAndIsDeletedFalse(conversation.getId(), lastReadAt);
    }

    private List<ParticipantInfo> createParticipants(List<String> ids) {
        Instant now = Instant.now();
        return userRepository.findAllById(ids).stream()
                .map(user -> ParticipantInfo.builder()
                        .userId(user.getId())
                        .displayName(user.getDisplayName())
                        .joinedAt(now)
                        .build())
                .toList();
    }

    private ConversationResponse toResponse(Conversation conversation) {
        List<ParticipantInfo> participantInfoResponse = conversation.getParticipants().stream()
                .filter(participantInfo -> participantInfo.getLeftAt() == null)
                .map(participantInfoMapper::toResponse)
                .toList();

        ChatMessage lastMessage = chatMessageRepository
                .findFirstByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(conversation.getId())
                .orElse(null);
        Instant lastActive = lastMessage != null ? lastMessage.getCreatedAt() : null;

        String userId = UserUtils.getCurrUserId();
        ConversationResponse response = ConversationResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .participantsHash(conversation.getParticipantHash())
                .participants(participantInfoResponse)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .name(conversation.getName())
                .createdBy(conversation.getCreatedBy())
                .lastActive(lastActive)
                .lastMessagePreview(MessageUtils.getMessagePreview(lastMessage))
                .unreadCount(getUnreadCount(conversation, userId))
                .build();

        if (conversation.getType() == ChatType.DIRECT) {
            participantInfoResponse.stream()
                    .filter(p -> !p.getUserId().equals(userId))
                    .findFirst().ifPresent(p -> {
                        response.setConvAvatar(userRepository.getAvatarUrlAndDisplayNameById(p.getUserId()).get().getAvatarUrl());
                        response.setName(p.getDisplayName());
                    });
        } else if (conversation.getType() == ChatType.GROUP) {
            response.setConvAvatar(conversation.getAvatarUrl());
        }
        return response;
    }

    private Conversation getValidGroupConversation(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        if (conversation.getType() == ChatType.DIRECT) {
            throw new AppException(ErrorCode.CONVERSATION_NOT_FOUND);
        }
        return conversation;
    }

}
