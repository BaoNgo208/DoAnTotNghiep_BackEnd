package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.dto.request.AddVideosRequest;
import com.example.spring_boot_react_demo.model.dto.request.ApplyTransitionRequest;
import com.example.spring_boot_react_demo.model.dto.request.VideoRequest;
import com.example.spring_boot_react_demo.model.dto.response.VideoResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface VideoService {
    List<VideoResponse> addVideo(AddVideosRequest addVideosRequest);
    String deleteVideo(Long id);
    void updateVideo(VideoRequest backgroundRequest);
    MultipartFile applyTransition (ApplyTransitionRequest applyTransitionRequest) throws IOException;
    MultipartFile removeEffect(Long projectId) throws IOException;
}
