package com.example.spring_boot_react_demo.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    boolean deleteFile(String publicId, String resourceType);

    default String uploadFile(MultipartFile file, String folderName, String resourceType) {
        return uploadFile(file, resourceType);
    }

    public String uploadFile(MultipartFile file, String resourceType);
}