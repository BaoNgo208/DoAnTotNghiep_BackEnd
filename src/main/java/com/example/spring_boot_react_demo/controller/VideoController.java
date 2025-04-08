package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.model.dto.request.ApplyTransitionRequest;
import com.example.spring_boot_react_demo.model.dto.response.ApiResponse;
import com.example.spring_boot_react_demo.model.dto.response.VideoResponse;
import com.example.spring_boot_react_demo.service.VideoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class VideoController {
    VideoService videoService;

    @PostMapping()
    public ApiResponse<List<VideoResponse>> addVideo(@RequestParam List<MultipartFile> files, @RequestParam Long projectId) {
        return ApiResponse.<List<VideoResponse>>builder()
                .result(videoService.addVideo( files, projectId))
                .build();
    }

    @DeleteMapping()
    public ApiResponse<String> deleteVideo(@RequestParam Long videoId) {
        return ApiResponse.<String>builder()
                .result(videoService.deleteVideo(videoId))
                .build();
    }

    @PostMapping("/applyTransition")
    public ApiResponse<?> applyTransition(@RequestBody ApplyTransitionRequest applyTransitionRequest) throws IOException {

        MultipartFile result = videoService.applyTransition(applyTransitionRequest);
        return ApiResponse.builder()
                .result(Base64.getEncoder().encodeToString(result.getBytes()))
                .build();
    }

    @PostMapping("/removeEffect")
    public ApiResponse<?> removeEffect(@RequestParam Long projectId) throws IOException {
        MultipartFile result = videoService.removeEffect(projectId);
        return ApiResponse.builder()
                .result(Base64.getEncoder().encodeToString(result.getBytes()))
                .build();
    }
}