package com.chat_app.repository;

import com.chat_app.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    @Query(value = "{ 'participants': { $elemMatch: { 'userId': ?0, 'leftAt': null } }, 'isDeleted': false }")
    List<Conversation> findAllActiveConversationsByUserId(String userId);
    Optional<Conversation> findByParticipantHash(String hash);
    Optional<Conversation> findByIdAndIsDeletedFalse(String id);
}
