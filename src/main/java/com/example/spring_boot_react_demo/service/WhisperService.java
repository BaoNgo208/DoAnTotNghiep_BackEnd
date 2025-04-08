package com.example.spring_boot_react_demo.service;

import org.springframework.web.multipart.MultipartFile;

public interface WhisperService {
    MultipartFile transcribeAudio(MultipartFile audioFile);
    String processVideo(MultipartFile videoFile, Long projectId);
}