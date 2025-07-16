package com.chat_app.repository;

import com.chat_app.model.Notification;
import com.chat_app.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByReceiverAndIsReadFalseAndIsDeletedFalse(User receiver);
    List<Notification> findByReceiverAndIsDeletedFalse(User receiver);
    Long countByReceiverAndIsReadAndDeletedFalse(User receiver);
}

