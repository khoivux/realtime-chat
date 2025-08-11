package com.chat_app.service.common;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    String uploadFile(MultipartFile file);
    void deleteFile(String imageUrl);
}
