package com.chat_app.service;

import com.chat_app.constant.ChatType;
import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.request.ConversationRequest;
import com.chat_app.dto.request.ParticipantRequest;
import com.chat_app.dto.request.UpdateConversationRequest;
import com.chat_app.dto.response.ConversationResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.mapper.ParticipantInfoMapper;
import com.chat_app.model.Conversation;
import com.chat_app.model.ParticipantInfo;
import com.chat_app.model.User;
import com.chat_app.repository.ConversationRepository;
import com.chat_app.repository.UserRepository;
import com.chat_app.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService{
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ParticipantInfoMapper participantInfoMapper;

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
    public ConversationResponse create(ConversationRequest request) {
        String creatorId = UserUtils.getCurrUserId();

        List<String> userIds = new ArrayList<>(request.getParticipantIds());
        if (!userIds.contains(creatorId)) {
            userIds.add(creatorId);
        }

        List<String> sortedIds = userIds.stream()
                .distinct()
                .sorted()
                .toList();

        String userIdHash = generateParticipantHash(sortedIds);
        Conversation conversation = conversationRepository.findByParticipantHash(userIdHash)
                .orElseGet(() -> {
                    // Create participant list
                    List<ParticipantInfo> participants = createParticipants(sortedIds, creatorId);
                    if (participants.isEmpty()) {
                        throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                    }
                    Conversation newConversation = Conversation.builder()
                            .type(request.getType())
                            .createdBy(creatorId)
                            .participantHash(userIdHash)
                            .participants(participants)
                            .build();
                    if(request.getType().equals(ChatType.GROUP.name())) {
                        newConversation.setName(participants.getFirst().getDisplayName() + ", " + participants.getLast().getDisplayName());
                    }
                    return conversationRepository.save(newConversation);
                });

        return toResponse(conversation);
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
        validPermission(conversation, UserUtils.getCurrUserId());
        if(conversation.getType().equals(ChatType.DIRECT.name())) {
            throw new AppException(ErrorCode.CONVERSATION_NOT_FOUND);
        }
        conversation.setName(request.getName());
        // any fields
        //
        return toResponse(conversationRepository.save(conversation));
    }

    /**
     * Adds a user to or removes a user from a conversation.
     *
     * @param request participant action request
     */
    @Override
    public void addOrDeleteUser(ParticipantRequest request) {
        String currUserId = UserUtils.getCurrUserId();
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        if(conversation.getType().equals(ChatType.DIRECT.name())) {
            throw new AppException(ErrorCode.CONVERSATION_NOT_FOUND);
        }

        List<ParticipantInfo> participants = conversation.getParticipants();
        validPermission(conversation, currUserId);

        if(request.isJoin()) {
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
        } else {
            if(!conversation.getCreatedBy().equals(currUserId)) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
            ParticipantInfo participant = conversation.getParticipants().stream()
                    .filter(p -> p.getUserId().equals(request.getUserId()))
                    .findAny()
                    .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED));

            participant.setLeftAt(Instant.now());
        }
        conversationRepository.save(conversation);
    }

    /**
     * Generates a hash string from sorted participant IDs.
     *
     * @param participantIds list of participant IDs
     * @return participant hash string
     */
    private String generateParticipantHash(List<String> participantIds) {
        StringJoiner stringJoiner = new StringJoiner("_");
        participantIds.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }

    private List<ParticipantInfo> createParticipants(List<String> ids, String adminId) {
        Instant now = Instant.now();
        return userRepository.findAllById(ids).stream()
                .map(user -> ParticipantInfo.builder()
                        .userId(user.getId())
                        .displayName(user.getDisplayName())
                        .joinedAt(now)
                        .build())
                .toList();
    }

    /**
     * Converts ChatMessage to ChatMessageResponse.
     * Includes parent message info and sender details.
     *
     * @param conversation the message to convert
     * @return ChatMessageResponse
     */
    private ConversationResponse toResponse(Conversation conversation) {
        List<ParticipantInfo> participantInfoResponse = conversation.getParticipants().stream()
                .filter(participantInfo -> participantInfo.getLeftAt() == null)
                .map(participantInfoMapper::toResponse)
                .toList();

        ConversationResponse response = ConversationResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .participantsHash(conversation.getParticipantHash())
                .participants(participantInfoResponse)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .name(conversation.getName())
                .build();

        User currUser = UserUtils.getCurrUser();
        if (conversation.getType().equals(ChatType.DIRECT.name())) {
            participantInfoResponse.stream()
                    .filter(p -> !p.getUserId().equals(currUser.getId()))
                    .findFirst().ifPresent(p -> {
                        response.setConvAvatar(userRepository.getAvatarUrlAndDisplayNameById(p.getUserId()).get().getAvatarUrl());
                        response.setName(p.getDisplayName());
                    });
        }
        return response;
    }

    /**
     * Validates that the current user is an active participant of the conversation.
     *
     * @param conversation the conversation
     * @param currUserId the ID of the current user
     */
    private void validPermission(Conversation conversation, String currUserId) {
        conversation.getParticipants().stream()
                .filter(participant ->
                        participant.getUserId().equals(currUserId)
                                && participant.getLeftAt() == null
                )
                .findAny()
                .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED));
    }
}
