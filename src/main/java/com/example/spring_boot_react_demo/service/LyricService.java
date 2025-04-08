package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.dto.response.LyricResponse;
import com.example.spring_boot_react_demo.model.entity.Video;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

public interface LyricService {
    List<String> getLyricsByProjectId(Long projectId);
    Optional<LyricResponse> getLyricById(Long lyricId);
    void deleteLyric(Long projectId);
    LyricResponse showAndHideLyrics(Long projectId, MultipartFile file);
    MultipartFile addLyricToVideo(MultipartFile videoFile, Long projectId);
    MultipartFile updateLyric(Long lyricId, String newLyric, MultipartFile file);
    MultipartFile applyKaraEffect(MultipartFile videoFile, Long projectId) throws IOException;
    String cutLyricsByTimeRange(TreeMap<Integer, Video> videosMap, String text, Double duration);
    String convertEffectTextToOriginal(List<Video> videoList, String text, Double duration);
    }
