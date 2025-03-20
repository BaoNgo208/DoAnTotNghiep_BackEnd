package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.dto.response.LyricResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface LyricService {
    List<String> getLyricsByProjectId(Long projectId);
    Optional<LyricResponse> getLyricById(Long lyricId);
    MultipartFile updateLyric(Long lyricId, String newLyric, MultipartFile file);
    MultipartFile addLyricToVideo(MultipartFile videoFile, Long projectId);
}
