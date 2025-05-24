package com.example.spring_boot_react_demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface CloudinaryService {
    void deleteFile(String publicId, String resourceType);
    String uploadFile(MultipartFile file, String resourceType);
    String uploadFile(String url) throws IOException;
     String uploadFile(File file, String resourceType);
}