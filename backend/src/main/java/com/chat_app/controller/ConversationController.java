package com.chat_app.controller;

import com.chat_app.dto.request.ConversationRequest;
import com.chat_app.dto.request.ParticipantRequest;
import com.chat_app.dto.response.ApiResponse;
import com.chat_app.dto.response.ConversationResponse;
import com.chat_app.service.ConversationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j(topic = "CONVERSATION-CONTROLLER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/conversation")
@Tag(name = "Conversation Controller")
public class ConversationController {
    private final ConversationService conversationService;

    @PostMapping("/")
    public ApiResponse<ConversationResponse> create(@Valid @RequestBody ConversationRequest request) {
        return ApiResponse.<ConversationResponse>builder()
                .data(conversationService.create(request))
                .message("Tạo phiên chat thành công")
                .build();
    }

    @GetMapping("/")
    public ApiResponse<?> getMyConversations() {
        return ApiResponse.builder()
                .data(conversationService.myConversations())
                .message("Lấy danh sách phiên chat thành công")
                .build();
    }

    @GetMapping("/{conversationId}")
    public ApiResponse<?> getMyConversations(@PathVariable String conversationId) {
        return ApiResponse.builder()
                .data(conversationService.getConversation(conversationId))
                .message("Lấy danh sách phiên chat thành công")
                .build();
    }

    @PatchMapping("/")
    public ApiResponse<?> addOrDeleteUser(@RequestBody ParticipantRequest request) {
        log.info("Request isJoin: {}", request.isJoin());
        conversationService.addOrDeleteUser(request);
        return ApiResponse.builder()
                .message("Lấy danh sách phiên chat thành công")
                .build();
    }
}
