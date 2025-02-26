package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.dto.request.BackgroundRequest;
import com.example.spring_boot_react_demo.model.dto.response.BackgroundResponse;
import com.example.spring_boot_react_demo.model.entity.Background;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BackgroundService {
    public List<BackgroundResponse> addBackground(List<MultipartFile> files, Long projectId);
    public String deleteBackground(Long id);
    public Background updateBackground(BackgroundRequest backgroundRequest);
}