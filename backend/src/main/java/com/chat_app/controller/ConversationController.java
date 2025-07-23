package com.chat_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat_app.dto.request.ConversationRequest;
import com.chat_app.dto.request.ParticipantRequest;
import com.chat_app.dto.request.UpdateConversationRequest;
import com.chat_app.dto.response.ApiResponse;
import com.chat_app.dto.response.ConversationResponse;
import com.chat_app.service.ConversationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    public ApiResponse<?> getConversationById(@PathVariable String conversationId) {
        return ApiResponse.builder()
                .data(conversationService.getConversation(conversationId))
                .message("Lấy thông tin phiên chat thành công")
                .build();
    }

    @PutMapping("/")
    public ApiResponse<?> update(@RequestBody UpdateConversationRequest request) {
        return ApiResponse.builder()
                .data(conversationService.update(request))
                .message("Cập nhật phiên chat thành công")
                .build();
    }

    @PatchMapping("/add-user")
    public ApiResponse<?> addParticipant(@RequestBody ParticipantRequest request) {
        conversationService.addParticipant(request);
        return ApiResponse.builder()
                .message("Thêm người dùng thành công")
                .build();
    }

    @PatchMapping("/remove-user")
    public ApiResponse<?> removeParticipant(@RequestBody ParticipantRequest request) {
        conversationService.removeParticipant(request);
        return ApiResponse.builder()
                .message("Xóa người dùng thành công")
                .build();
    }
}
