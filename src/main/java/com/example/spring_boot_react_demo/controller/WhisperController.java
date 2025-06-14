package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.service.WhisperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class WhisperController {

    private final WhisperService whisperService;

    @PostMapping("/process")
    public ResponseEntity<?> processVideo(@RequestParam("file") MultipartFile videoFile,
                                          @RequestParam("projectId") Long projectId,
                                          @RequestParam("targetLang") String targetLang
    ) {
        if (videoFile.isEmpty()) {
            return ResponseEntity.badRequest().body("No video file provided.");
        }
        try {
            String uploadedUrl = whisperService.processVideo(videoFile, projectId,targetLang);
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

    @GetMapping("/translate")
    public String translate(
            @RequestParam String text,
            @RequestParam(defaultValue = "auto") String sourceLang,
            @RequestParam String targetLang) {

        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://translate.googleapis.com/translate_a/single?client=gtx&sl=%s&tl=%s&dt=t&q=%s",
                    sourceLang, targetLang, encodedText
            );

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response = in.lines().collect(Collectors.joining());
                return response.split("\"")[1]; // basic parsing
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Translation failed: " + e.getMessage();
        }
    }
}