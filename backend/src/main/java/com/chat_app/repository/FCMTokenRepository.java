package com.chat_app.repository;

import com.chat_app.model.FCMToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FCMTokenRepository extends MongoRepository<FCMToken, String> {
    Optional<FCMToken> findById(String userId);
}

