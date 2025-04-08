package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.model.LyricSegment;
import com.example.spring_boot_react_demo.model.dto.response.LyricResponse;
import com.example.spring_boot_react_demo.model.entity.Lyric;
import com.example.spring_boot_react_demo.model.entity.Project;
import com.example.spring_boot_react_demo.model.entity.Video;
import com.example.spring_boot_react_demo.repository.LyricRepo;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.FFmpegService;
import com.example.spring_boot_react_demo.service.LyricService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import static com.example.spring_boot_react_demo.util.AssUtil.*;
import static com.example.spring_boot_react_demo.util.Constants.MP4;
import static com.example.spring_boot_react_demo.util.Constants.OUTPUT_VIDEO_FILE;
import static com.example.spring_boot_react_demo.util.FileUtil.deleteFileIfExists;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

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

    @Override
    public MultipartFile applyKaraEffect(MultipartFile videoFile, Long projectId) throws IOException {
        Lyric lyric =  lyricRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Lyric not found for projectId: " + projectId));

        lyric.setText(modifyASSContent(lyric.getText()));
        lyricRepository.save(lyric);
        return ffmpegService.addAssToVideo(videoFile, createAssFile(lyric.getText()));
    }

    @Override
    public String cutLyricsByTimeRange(TreeMap<Integer, Video> videosMap, String text, Double duration){
        List<Integer> keys = new ArrayList<>(videosMap.keySet());
        String result = null;
        for (int i = 1 ; i < keys.size() ; i++) {
            double cutStart = videosMap.get(i-1).getEndTime() - (duration * i);
            double cutEnd = videosMap.get(i).getStartTime() + duration * (2 - i);
            result = cutLyricsByTimeRange(text, cutStart, cutEnd);
        }
        return result;
    }


    private String cutLyricsByTimeRange(String text, Double cutStart, Double cutEnd) {
        List<LyricSegment> lyricSegments = convertAssTextToList(text);
        List<LyricSegment> result = new ArrayList<>();
        Double duration = (cutEnd - cutStart) / 2;
        for (LyricSegment lyric : lyricSegments) {
            double startTime = lyric.getStartTime();
            double endTime = lyric.getEndTime();
            if(endTime < cutStart ) {
                result.add(lyric);
            }
            else if(startTime > cutEnd){
                lyric.setStartTime(startTime - duration);
                lyric.setEndTime(endTime - duration);
                result.add(lyric);
            }
        }
        return convertListToAssText(result, text);
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