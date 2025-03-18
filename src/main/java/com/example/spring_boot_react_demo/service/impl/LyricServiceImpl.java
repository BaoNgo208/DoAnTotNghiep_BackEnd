package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.model.dto.response.LyricResponse;
import com.example.spring_boot_react_demo.model.entity.Lyric;
import com.example.spring_boot_react_demo.repository.LyricRepo;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.FFmpegService;
import com.example.spring_boot_react_demo.service.LyricService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import static com.example.spring_boot_react_demo.util.FileUtil.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
                        (lyric.getProject() != null) ? lyric.getProject().getId() : null,
                        lyric.isLyricHidden()));
    }

    @Override
    public MultipartFile updateLyric(Long projectId, String newLyric, MultipartFile file) {
        Lyric lyric = lyricRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Lyric not found for projectId: " + projectId));
        lyric.setText(newLyric.trim());
        lyricRepository.save(lyric);
        return ffmpegService.addAssToVideo(file, createAssFile(lyric.getText()));
    }

    @Override
    public LyricResponse showAndHideLyrics(Long projectId, MultipartFile file) {
        Lyric lyric = lyricRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Lyric not found for project " + projectId));
        boolean newHiddenState = !lyric.isLyricHidden();
        if (newHiddenState) {
            lyric.setOriginalText(lyric.getText());
            lyric.setText("");
        } else {
            if (lyric.getOriginalText() != null && !lyric.getOriginalText().isEmpty()) {
                lyric.setText(lyric.getOriginalText());
            }
        }
        lyric.setLyricHidden(newHiddenState);
        lyricRepository.save(lyric);
        String videoUrl = updateLyricForHiddenLyrics(projectId,lyric.getText(),file);
        return new LyricResponse(
                lyric.getId(),
                lyric.getText(),
                lyric.getProject().getId(),
                lyric.isLyricHidden(),
                videoUrl
        );
    }

    @Override
    public void deleteLyric(Long projectId) {
        Lyric lyric = lyricRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Lyric not found for projectId: " + projectId));
        lyricRepository.delete(lyric);
    }

    @Override
    public MultipartFile addLyricToVideo(MultipartFile videoFile, Long projectId) {
        Lyric lyric = lyricRepository.findByProjectId(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_HAS_NO_LYRICS));
        return ffmpegService.addAssToVideo(videoFile, createAssFile(lyric.getText()));
    }

    private File createMinimalAssFile() throws IOException {
        String minimalSubtitle = "1\n00:00:00,000 --> 00:00:05,000\n ";
        File file = File.createTempFile("lyrics", ".ass");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(minimalSubtitle);
        }
        return file;
    }

    public String updateLyricForHiddenLyrics(Long projectId, String newText, MultipartFile file) {
        Lyric lyric = lyricRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Lyric not found for projectId: " + projectId));
        lyric.setText(newText.trim());
        lyricRepository.save(lyric);

        try {
            File assFile;
            if (lyric.getText().trim().isEmpty()) {
                assFile = createMinimalAssFile();
            } else {
                assFile = createAssFile(lyric.getText());
            }
            MultipartFile processedVideo = ffmpegService.addAssToVideo(file, assFile);
            if (processedVideo == null) {
                throw new IOException("Failed to generate video with subtitles for: " + file.getOriginalFilename());
            }
            return cloudinaryService.uploadFile(processedVideo,"video");

        } catch (IOException e) {
            return "Lyric updated, but failed to create video with subtitles";
        }
    }
}