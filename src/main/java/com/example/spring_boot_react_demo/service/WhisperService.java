package com.example.spring_boot_react_demo.service;

import org.springframework.web.multipart.MultipartFile;

public interface WhisperService {
    public MultipartFile transcribeAudio(MultipartFile audioFile);
    public String processVideo(MultipartFile videoFile, Long projectId);
}