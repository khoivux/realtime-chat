package com.chat_app.mapper;

import com.chat_app.dto.request.ChatMessageRequest;
import com.chat_app.dto.response.ChatMessageResponse;
import com.chat_app.model.ChatMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);
    ChatMessage toChatMessage(ChatMessageRequest request);
}