package com.chat_app.repository;

import com.chat_app.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    @Query(value = "{ 'conversationId': ?0, 'isDeleted': false }", sort = "{ 'createdDate': -1 }")
    List<ChatMessage> findByConversationId(String conversationId);
    Optional<ChatMessage> findByIdAndIsDeletedFalse(String id);
}
