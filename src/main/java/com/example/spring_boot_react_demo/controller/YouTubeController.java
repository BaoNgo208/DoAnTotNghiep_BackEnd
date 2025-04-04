package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.service.impl.YouTubeService;
import com.mysql.cj.util.StringUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.spring_boot_react_demo.service.ProjectService;
import java.util.Map;

@RestController
@RequestMapping("/youtube")
public class YouTubeController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private YouTubeService youTubeService;

    @PostMapping("/getAccessToken")
    public ResponseEntity<?> getAccessToken(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            String accessToken = youTubeService.fetchAccessToken(code);
            return ResponseEntity.ok(Map.of("access_token", accessToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("projectId") Long projectId
    ) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");

            String videoUrl = projectService.getProjectById(projectId).getAsset();
            if (StringUtils.isNullOrEmpty(videoUrl)) {
                return ResponseEntity.badRequest().body("No video found for projectId: " + projectId);
            }

            String videoId = youTubeService.uploadToYouTube(accessToken, videoUrl);
            return ResponseEntity.ok(Map.of("videoId", videoId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
