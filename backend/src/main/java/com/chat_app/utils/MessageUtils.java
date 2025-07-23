package com.chat_app.utils;

import com.chat_app.model.ChatMessage;
import com.chat_app.model.ParticipantInfo;

public class MessageUtils {

    private MessageUtils() {
    }

    public static String getMessagePreview(ChatMessage message) {
        if(message == null) {
            return null;
        }
        /*
            media type
         */
        ParticipantInfo sender = message.getSender();
        String senderPreviewName = sender.getUserId().equals(UserUtils.getCurrUserId())
                ? "You" : sender.getDisplayName();
        return senderPreviewName + ": " + message.getMessage();
    }
}
