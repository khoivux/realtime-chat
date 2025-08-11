package com.chat_app.service.common;


import com.chat_app.constant.ErrorCode;
import com.chat_app.exception.custom.FileException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {
    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            log.info("Image URL: {}", uploadResult.get("url").toString());
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new FileException(ErrorCode.UPLOAD_FAIL);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            Map result = cloudinary.uploader().destroy(extractPublicId(fileUrl), ObjectUtils.emptyMap());
            log.info("Delete Result: {}", result.get("result"));
        } catch (IOException e) {
            throw new FileException(ErrorCode.DELETE_FAIL);
        }

    }

    private String extractPublicId(String fileUrl) {
        String[] parts = fileUrl.split("/");
        String fileName = parts[parts.length - 1];
        return fileName.split("\\.")[0];
    }
}
