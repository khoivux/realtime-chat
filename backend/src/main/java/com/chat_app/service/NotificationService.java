package com.chat_app.service;

import com.chat_app.model.Notification;

import java.util.List;

public interface NotificationService {

    void sendNotification(Notification notification);

    List<Notification> getNotifications();

    void markAllAsRead();

    Long countUnread();
}
