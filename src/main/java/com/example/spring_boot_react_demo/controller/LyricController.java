package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.model.dto.response.ApiResponse;
import com.example.spring_boot_react_demo.model.dto.response.LyricResponse;
import com.example.spring_boot_react_demo.model.dto.response.ApiResponse;
import com.example.spring_boot_react_demo.model.entity.Lyric;
import com.example.spring_boot_react_demo.service.LyricService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lyrics")
public class LyricController {
    @Autowired
    private LyricService lyricService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<String>> getLyricsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(lyricService.getLyricsByProjectId(projectId));
    }

    @GetMapping("/{lyricId}")
    public ResponseEntity<?> getLyricById(@PathVariable Long lyricId) {
        return lyricService.getLyricById(lyricId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping()
    public ApiResponse<?> updateLyric(@RequestParam("file") MultipartFile videoFile, @RequestParam("projectId") Long projectId, @RequestParam String text) throws IOException {
        MultipartFile resultFile = lyricService.updateLyric(projectId, text, videoFile);
        return ApiResponse.builder()
                .result(Base64.getEncoder().encodeToString(resultFile.getBytes()))
                .build();
    }

    @PostMapping("/addAssToVideo")
    public ApiResponse<?> addLyricToVideo(@RequestParam("file") MultipartFile videoFile, @RequestParam("projectId") Long projectId) throws IOException {
        MultipartFile resultFile = lyricService.addLyricToVideo(videoFile,projectId);
        return ApiResponse.builder()
                .result(Base64.getEncoder().encodeToString(resultFile.getBytes()))
                .build();
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<?> showAndHideLyrics(@PathVariable Long projectId, @RequestParam("file") MultipartFile videoFile) {
        try {
            LyricResponse updatedLyric = lyricService.showAndHideLyrics(projectId,videoFile);
            return ResponseEntity.ok(updatedLyric);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}