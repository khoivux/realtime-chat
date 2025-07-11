package com.chat_app.service;

import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.request.ConversationRequest;
import com.chat_app.dto.response.ConversationResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.mapper.ConversationMapper;
import com.chat_app.model.Conversation;
import com.chat_app.model.ParticipantInfo;
import com.chat_app.repository.ConversationRepository;
import com.chat_app.repository.UserRepository;
import com.chat_app.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService{
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    @Override
    public List<ConversationResponse> myConversations() {
        String currUserId = UserUtils.getCurrUserId();
        List<Conversation> conversations = conversationRepository.findAllActiveConversationsByUserId(currUserId);
        return conversations.stream().map(ConversationMapper::toResponse).toList();
    }

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
        // Xử lý chat 1-1 đã tồn tại
        Conversation conversation = conversationRepository.findByParticipantsHash(userIdHash)
                .orElseGet(() -> {
                    // Danh sách thành viên
                    List<ParticipantInfo> participants = createParticipants(sortedIds, creatorId);
                    if (participants.isEmpty()) {
                        throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                    }
                    // Khởi tạo
                    Conversation newConversation = Conversation.builder()
                            .type(request.getType())
                            .participantHash(userIdHash)
                            .participants(participants)
                            .build();
                    return conversationRepository.save(newConversation);
                });

        return ConversationMapper.toResponse(conversation);
    }

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
                        .avatarUrl(user.getAvatarUrl())
                        .displayName(user.getDisplayName())
                        .isAdmin(user.getId().equals(adminId))
                        .joinedAt(now)
                        .build())
                .collect(Collectors.toList());
    }
}
