package com.chat_app.controller;

import com.chat_app.constant.ErrorCode;
import com.chat_app.dto.request.ChatMessageRequest;
import com.chat_app.dto.response.ApiResponse;
import com.chat_app.dto.response.ChatMessageResponse;
import com.chat_app.exception.custom.AppException;
import com.chat_app.service.ChatMessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j(topic = "CHAT_CONTROLLER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat Message Controller")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest request, Authentication auth) {
        if(auth != null) {
            log.info(request.getMessage());
            log.info(auth.getName());
            request.setSenderId(auth.getName());
            chatMessageService.sendMessage(request);
        } else {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    @GetMapping("/{conversationId}")
    public ApiResponse<?> getByConversation(@PathVariable String conversationId) {
        List<ChatMessageResponse> responseList = chatMessageService.getMessagesByConversation(conversationId);
        return ApiResponse.builder()
                .data(responseList)
                .message("Lấy danh sách tin nhắn thành công")
                .build();
    }

    @PatchMapping("/")
    public ApiResponse<?> updateChatMessage(@Valid @RequestBody ChatMessageRequest request) {
        return ApiResponse.builder()
                .data(chatMessageService.update(request))
                .message("Cap nhat tin nhan thanh cong")
                .build();
    }

    @DeleteMapping("/{chatId}")
    public ApiResponse<?> deleteChatMessage(@PathVariable String chatId) {
        chatMessageService.delete(chatId);
        return ApiResponse.builder()
                .message("Xóa tin nhắn thành công")
                .build();
    }
}
