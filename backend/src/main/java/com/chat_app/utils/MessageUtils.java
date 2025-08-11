package com.chat_app.utils;

import com.chat_app.constant.MessageType;
import com.chat_app.model.ChatMessage;
import com.chat_app.model.ParticipantInfo;

public class MessageUtils {

    private MessageUtils() {
    }

    public static String getMessagePreview(ChatMessage message) {
        if(message == null) {
            return null;
        }

        ParticipantInfo sender = message.getSender();
        String senderPreviewName = sender.getUserId().equals(UserUtils.getCurrUserId())
                ? "Bạn" : sender.getDisplayName();
        if(message.getType().equals(MessageType.IMAGE)) {
            return senderPreviewName + " đã gửi ảnh";
        }
        return senderPreviewName + ": " + message.getMessage();
    }
}
