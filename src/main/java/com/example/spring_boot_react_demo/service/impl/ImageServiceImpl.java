package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.model.entity.ImageModel;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public ResponseEntity<Map<String, String>> uploadImage(ImageModel imageModel) {
        try {
            MultipartFile file = imageModel.getFile();
            String fileName = imageModel.getName();
            if (file == null || file.isEmpty() || fileName == null || fileName.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File or name is empty"));
            }

            String contentType = file.getContentType();
            String resourceType = "raw";
            if (contentType != null) {
                if (contentType.startsWith("image/")) {
                    resourceType = "image";
                } else if (contentType.startsWith("video/")) {
                    resourceType = "video";
                } else if (contentType.startsWith("audio/")) {
                    resourceType = "audio";
                }
            }

            String fileUrl = cloudinaryService.uploadFile(file, "folder_1", resourceType);
            if (fileUrl == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Upload failed"));
            }

            return ResponseEntity.ok(Map.of("url", fileUrl, "type", resourceType));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
}
