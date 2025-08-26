package com.chat_app.utils;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;

@Component
public class ParticipantUtils {
    public static String generateParticipantHash(List<String> participantIds) {
        StringJoiner stringJoiner = new StringJoiner("_");
        participantIds.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }
}
