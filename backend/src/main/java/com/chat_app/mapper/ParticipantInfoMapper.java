package com.chat_app.mapper;

import com.chat_app.model.ParticipantInfo;
import com.chat_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantInfoMapper {
    private final UserRepository userRepository;
    public ParticipantInfo toResponse(ParticipantInfo participantInfo) {
        userRepository.getAvatarUrlAndDisplayNameById(participantInfo.getUserId())
                .ifPresent(projection -> {
                    participantInfo.setAvatarUrl(projection.getAvatarUrl());
                    participantInfo.setDisplayName(projection.getDisplayName());
                });
        return participantInfo;
    }
}
