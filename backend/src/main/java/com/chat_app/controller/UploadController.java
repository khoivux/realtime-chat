package com.chat_app.controller;

import com.chat_app.dto.response.ApiResponse;
import com.chat_app.service.common.UploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
@Tag(name = "Upload Controller")
public class UploadController {
    private final UploadService uploadService;

    @PostMapping("/")
    public ApiResponse<?> uploadFile(@RequestParam("file") MultipartFile file) {
        return ApiResponse.builder()
                .data(uploadService.uploadFile(file))
                .build();
    }
}
