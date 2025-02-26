package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.service.LyricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
    public ResponseEntity<?> updateLyric(@RequestParam("file") MultipartFile videoFile, @RequestParam("projectId") Long projectId, @RequestParam String text) {
        String updatedLyric = lyricService.updateLyric(projectId, text, videoFile);
        return ResponseEntity.ok(Map.of("message", "Lyric updated successfully", "videoUrl", updatedLyric));
    }
}