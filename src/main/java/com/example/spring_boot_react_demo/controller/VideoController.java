package com.example.spring_boot_react_demo.controller;
import com.example.spring_boot_react_demo.service.FFmpegService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/video")
public class VideoController {
    private final FFmpegService videoService;

    @Autowired
    public VideoController(FFmpegService videoService) {
        this.videoService = videoService;
    }

    @PostMapping("/convert")
    public ResponseEntity<String> convertVideo(@RequestParam String inputVideoPath, @RequestParam String outputVideoPath) {
        try {
            String result = videoService.convertAndUploadVideo(inputVideoPath, outputVideoPath);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error while converting video: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}