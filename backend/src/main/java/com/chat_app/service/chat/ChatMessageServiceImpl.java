package com.chat_app.service.chat;

import com.chat_app.constant.Constants;
import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.request.ChatMessageRequest;
import com.chat_app.dto.response.ChatMessageResponse;
import com.chat_app.dto.response.OffsetResponse;
import com.chat_app.dto.response.stats.ChatStatsResponse;
import com.chat_app.dto.response.stats.HourlyMessageStats;
import com.chat_app.exception.custom.AppException;
import com.chat_app.mapper.ChatMessageMapper;
import com.chat_app.mapper.ParticipantInfoMapper;
import com.chat_app.model.ChatMessage;
import com.chat_app.model.ParticipantInfo;
import com.chat_app.model.User;
import com.chat_app.repository.ChatMessageRepository;
import com.chat_app.repository.ConversationRepository;
import com.chat_app.repository.UserRepository;
import com.chat_app.service.common.UploadService;
import com.chat_app.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService{
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final UploadService uploadService;
    private final ParticipantInfoMapper participantInfoMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final MongoTemplate mongoTemplate;


    @Override
    public OffsetResponse<ChatMessageResponse> getMessagesByConversation(String conversationId, int offset, int limit) {
        String userId = UserUtils.getCurrUserId();

        conversationService.checkConversationAccess(
                conversationRepository.findByIdAndIsDeletedFalse(conversationId)
                        .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND))
                , userId);

        OffsetScrollPosition position =  offset > 0 ? ScrollPosition.offset(offset - 1) : ScrollPosition.offset();
        List<ChatMessageResponse> messages = chatMessageRepository.getByConversationId(conversationId, position, Limit.of(limit))
                .getContent().stream()
                .map(this::toChatMessageResponse)
                .toList();

        return OffsetResponse.<ChatMessageResponse>builder()
                .content(messages)
                .nextOffset(messages.size() == limit ? offset + messages.size() : offset)
                .build();
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
        messagingTemplate.convertAndSend(Constants.TOPIC_CONVERSATIONS_PREFIX + response.getConversationId(), response);
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
        messagingTemplate.convertAndSend(Constants.TOPIC_CONVERSATIONS_PREFIX  + response.getConversationId(), response);
    }

    @Override
    @Transactional
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
        messagingTemplate.convertAndSend(Constants.TOPIC_CONVERSATIONS_PREFIX  + request.getConversationId(), response);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ChatStatsResponse getStats(LocalDate date) {
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");

        Instant startOfDay = date.atStartOfDay(vietnamZone).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(vietnamZone).toInstant();

        System.out.println("Start of day: " + startOfDay); // 2025-08-19T00:00:00Z tương ứng với zone
        System.out.println("End of day: " + endOfDay);     // 2025-08-20T00:00:00Z
        long dailyMessages = chatMessageRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(startOfDay).lt(endOfDay)),
                Aggregation.project()
                        .and(DateOperators.DateToString.dateOf("createdAt")
                                .toString("%H")
                                .withTimezone(DateOperators.Timezone.valueOf("Asia/Ho_Chi_Minh")))
                        .as("hour"),
                Aggregation.group("hour").count().as("count"),
                Aggregation.project("count").and("_id").as("hour"),
                Aggregation.sort(Sort.by("hour").ascending())
        );




        List<HourlyMessageStats> hourlyStats = mongoTemplate.aggregate(
                agg, "chat_message", HourlyMessageStats.class
        ).getMappedResults();

        Map<Integer, Long> statsMap = hourlyStats.stream()
                .collect(Collectors.toMap(HourlyMessageStats::getHour, HourlyMessageStats::getCount));

        List<HourlyMessageStats> fullHourlyStats = IntStream.range(0, 24)
                .mapToObj(hour -> new HourlyMessageStats(hour, statsMap.getOrDefault(hour, 0L)))
                .toList();

        return new ChatStatsResponse(dailyMessages, fullHourlyStats);
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
