package com.example.spring_boot_react_demo.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file, String resourceType);
    boolean deleteFile(String publicId, String resourceType);
    String export(MultipartFile file, String folderName, String resourceType);
    boolean deleteFile(String publicId, String resourceType);
}