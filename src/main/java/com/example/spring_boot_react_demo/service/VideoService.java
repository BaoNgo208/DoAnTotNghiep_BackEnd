package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.dto.request.VideoRequest;
import com.example.spring_boot_react_demo.model.dto.response.VideoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {
    List<VideoResponse> addVideo(List<MultipartFile> files, Long projectId);
    String deleteVideo(Long id);
    void updateVideo(VideoRequest backgroundRequest);
}
