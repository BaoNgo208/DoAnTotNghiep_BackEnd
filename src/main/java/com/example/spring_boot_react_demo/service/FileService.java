package com.example.spring_boot_react_demo.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    public boolean isValidFileFormat(MultipartFile file);
}