package com.chat_app.repository;

import com.chat_app.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Optional<Conversation> findByParticipantHash(String hash);
    @Query("{'participants.userId' : ?0, 'isDeleted': false}")
    List<Conversation> findAllActiveConversationsByUserId(String userId);
    Optional<Conversation> findByIdAndIsDeletedFalse(String id);
}
