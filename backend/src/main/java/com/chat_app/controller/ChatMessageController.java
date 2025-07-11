package com.chat_app.controller;

import com.chat_app.dto.request.ChatMessageRequest;
import com.chat_app.dto.response.ApiResponse;
import com.chat_app.service.ChatMessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat Message Controller")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @PostMapping("/")
    public ApiResponse<?> create(@Valid @RequestBody ChatMessageRequest request) {
        return ApiResponse.builder()
                .data(chatMessageService.create(request))
                .message("Tạo tin nhắn thành công")
                .build();
    }

    @GetMapping("/{conversationId}")
    public ApiResponse<?> getByConversation(@PathVariable String conversationId) {
        return ApiResponse.builder()
                .data(chatMessageService.getMessagesByConversation(conversationId))
                .message("Lấy danh sách tin nhắn thành công")
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
