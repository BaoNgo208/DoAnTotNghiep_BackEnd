package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.model.dto.response.LyricResponse;
import com.example.spring_boot_react_demo.model.entity.Lyric;
import com.example.spring_boot_react_demo.repository.LyricRepo;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.FFmpegService;
import com.example.spring_boot_react_demo.service.LyricService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;
import static com.example.spring_boot_react_demo.util.FileUtil.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LyricServiceImpl implements LyricService {

    LyricRepo lyricRepository;
    FFmpegService ffmpegService;
    CloudinaryService cloudinaryService;

    @Override
    public List<String> getLyricsByProjectId(Long projectId) {
        return lyricRepository.findByProjectId(projectId)
                .stream()
                .map(Lyric::getText)
                .toList();
    }

    public Optional<LyricResponse> getLyricById(Long lyricId) {
        return lyricRepository.findById(lyricId)
                .map(lyric -> new LyricResponse(
                        lyric.getId(),
                        lyric.getText(),
                        (lyric.getProject() != null) ? lyric.getProject().getId() : null
                ));
    }

    @Override
    public MultipartFile updateLyric(Long projectId, String newLyric, MultipartFile file) {
        Lyric lyric = lyricRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Lyric not found for projectId: " + projectId));
        lyric.setText(newLyric.trim());
        lyricRepository.save(lyric);
        return ffmpegService.addSrtToVideo(file, createSrcFile(lyric.getText()));
    }

    @Override
    public MultipartFile addLyricToVideo(MultipartFile videoFile, Long projectId) {
        Lyric lyric = lyricRepository.findByProjectId(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_HAS_NO_LYRICS));
        return ffmpegService.addSrtToVideo(videoFile, createSrcFile(lyric.getText()));
    }
}