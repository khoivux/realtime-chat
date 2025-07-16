package com.chat_app.service;

import com.chat_app.model.Notification;
import com.chat_app.model.User;
import com.chat_app.repository.NotificationRepository;
import com.chat_app.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotification(Notification notification) {
        notificationRepository.save(notification);
        messagingTemplate.convertAndSendToUser(
                String.valueOf(notification.getReceiver().getId()),
                "/notification",
                notification
        );
    }

    @Override
    public List<Notification> getNotifications() {
        User receiver = UserUtils.getCurrUser();
        return notificationRepository.findByReceiverAndIsDeletedFalse(receiver);
    }

    @Override
    public void markAllAsRead() {
        User receiver = UserUtils.getCurrUser();
        List<Notification> notifications = notificationRepository
                .findByReceiverAndIsReadFalseAndIsDeletedFalse(receiver);
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    public Long countUnread() {
        User receiver = UserUtils.getCurrUser();
        return notificationRepository.countByReceiverAndIsReadAndDeletedFalse(receiver);
    }
}
