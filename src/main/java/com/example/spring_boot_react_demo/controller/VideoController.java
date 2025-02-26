package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.model.dto.response.ApiResponse;
import com.example.spring_boot_react_demo.model.dto.response.VideoResponse;
import com.example.spring_boot_react_demo.service.FFmpegService;
import com.example.spring_boot_react_demo.service.VideoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class VideoController {
    @Autowired
    VideoService videoService;

    @Autowired
    FFmpegService fFmpegService;

    @PostMapping()
    public ApiResponse<List<VideoResponse>> addBackground(@RequestParam List<MultipartFile> files, @RequestParam Long projectId) {
        return ApiResponse.<List<VideoResponse>>builder()
                .result(videoService.addVideo( files, projectId))
                .build();
    }

    @DeleteMapping()
    public ApiResponse<String> deleteBackground(@RequestParam Long videoId) {
        return ApiResponse.<String>builder()
                .result(videoService.deleteVideo(videoId))
                .build();
    }

    @PostMapping("/convert")
    public ResponseEntity<String> convertVideo(@RequestParam("file") MultipartFile inputVideo, @RequestParam("outputFileExtension") String outputFormat) {
        try {
            String result = fFmpegService.convertVideo(inputVideo, outputFormat);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error while converting video: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}