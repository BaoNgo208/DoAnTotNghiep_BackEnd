package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.model.dto.response.LyricResponse;
import com.example.spring_boot_react_demo.model.entity.Lyric;
import com.example.spring_boot_react_demo.repository.LyricRepo;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.FFmpegService;
import com.example.spring_boot_react_demo.service.LyricService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LyricServiceImpl implements LyricService {

    @Autowired
    LyricRepo lyricRepository;
    @Autowired
    FFmpegService ffmpegService;
    @Autowired
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
    public String updateLyric(Long projectId, String newText, MultipartFile file) {
        Lyric lyric = lyricRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Lyric not found for projectId: " + projectId));
        lyric.setText(newText.trim());
        lyricRepository.save(lyric);

        try {
            MultipartFile srtFile = createSrtFile(lyric.getText());
            MultipartFile processedVideo = ffmpegService.addSrtToVideo(file, srtFile);
            if (processedVideo == null) {
                throw new IOException("Failed to generate video with subtitles for: " + file.getOriginalFilename());
            }
            return cloudinaryService.uploadFile(processedVideo,"video");

        } catch (IOException e) {
            return "Lyric updated, but failed to create video with subtitles";
        }
    }

    private MultipartFile createSrtFile(String text) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.write(text);
        }
        return new MockMultipartFile("lyrics.srt", "lyrics.srt", "text/plain", outputStream.toByteArray());
    }
}