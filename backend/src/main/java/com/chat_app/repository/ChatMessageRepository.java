package com.chat_app.repository;

import com.chat_app.model.ChatMessage;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    @Query(value = "{ 'conversationId': ?0, 'isDeleted': false }", sort = "{ 'createdAt': -1 }")
    Window<ChatMessage> getByConversationId(String conversationId, OffsetScrollPosition position, Limit limit);
    long countByConversationIdAndCreatedAtAfterAndIsDeletedFalse(String conversationId, Instant createdAt);
    Optional<ChatMessage> findFirstByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(String conversationId);
    Optional<ChatMessage> findByIdAndIsDeletedFalse(String id);
}
