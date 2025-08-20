package com.chat_app.service.common;

import com.chat_app.constant.ChatType;
import com.chat_app.model.ChatMessage;
import com.chat_app.model.Conversation;
import com.chat_app.model.ParticipantInfo;
import com.chat_app.repository.FCMTokenRepository;
import com.chat_app.service.user.UserService;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{
    private final FCMTokenRepository fcmTokenRepository;
    private final UserService userService;

    @Override
    public void sendToConversation(Conversation conversation, ChatMessage chatMessage) {
        List<String> onlineUserIds = userService.getOnlineUserIds();

        List<String> offlineParticipantIds = conversation.getParticipants().stream()
                .map(ParticipantInfo::getUserId)
                .filter(id -> !id.equals(chatMessage.getSender().getUserId()))
                .filter(id -> !onlineUserIds.contains(id))
                .toList();

        List<String> tokens = offlineParticipantIds.stream()
                .map(fcmTokenRepository::findById)
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getToken())
                .toList();
        log.info("[Notification] FCM tokens for offline participants: {}", tokens);

        if (!tokens.isEmpty()) {
            try {
                String title = conversation.getType().equals(ChatType.GROUP)
                        ? "Tin nhắn mới từ " + conversation.getName()
                        : "Tin nhắn mới";

                MulticastMessage message = MulticastMessage.builder()
                        .addAllTokens(tokens)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(chatMessage.getSender().getDisplayName() + ": "
                                        + chatMessage.getMessage())
                                .build())
                        .build();

                BatchResponse response = FirebaseMessaging.getInstance()
                        .sendEachForMulticast(message);
                log.info("[Notification] Send result: success={}, failure={}",
                        response.getSuccessCount(), response.getFailureCount());

            } catch (FirebaseMessagingException e) {
                log.error("Send Notification Failed");
            }
        }
    }
}
