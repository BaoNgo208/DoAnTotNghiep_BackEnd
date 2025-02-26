package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.service.WhisperService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WhisperController {

    @Autowired
    private final WhisperService whisperService;

    @PostMapping("/process")
    public ResponseEntity<?> processVideo(@RequestParam("file") MultipartFile videoFile) {
        if (videoFile.isEmpty()) {
            return ResponseEntity.badRequest().body("No video file provided.");
        }
        try {
            String uploadedUrl = whisperService.processVideo(videoFile);
            if (uploadedUrl == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to process video.");
            }
            return ResponseEntity.ok(Map.of("videoUrl", uploadedUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing video: " + e.getMessage());
        }
    }
}