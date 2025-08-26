package com.chat_app.repository;

import com.chat_app.model.User;
import com.chat_app.repository.projection.ParticipantInfoProjection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query(value = "{ '_id': ?0 }", fields = "{ 'avatarUrl': 1, 'displayName': 1 }")
    Optional<ParticipantInfoProjection> getAvatarUrlAndDisplayNameById(String userId);
    @Query("{ $or: [ { 'username': { $regex: ?0, $options: 'i' } }, { 'displayName': { $regex: ?0, $options: 'i' } } ] }")
    List<User> findByUsernameOrDisplayName(String keyword);
    long countByIsBlockedTrue();
    long countByCreatedAtBetween(Instant start, Instant end);
}
