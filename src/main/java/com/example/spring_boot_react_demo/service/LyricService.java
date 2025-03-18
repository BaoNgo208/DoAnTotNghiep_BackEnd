package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.dto.response.LyricResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface LyricService {
    List<String> getLyricsByProjectId(Long projectId);
    Optional<LyricResponse> getLyricById(Long lyricId);
    void deleteLyric(Long projectId);
    LyricResponse showAndHideLyrics(Long projectId, MultipartFile file);
    MultipartFile addLyricToVideo(MultipartFile videoFile, Long projectId);
    MultipartFile updateLyric(Long lyricId, String newLyric, MultipartFile file);
}
