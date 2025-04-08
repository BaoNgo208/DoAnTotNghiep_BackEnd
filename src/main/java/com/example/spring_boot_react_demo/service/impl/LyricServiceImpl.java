package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.model.LyricSegment;
import com.example.spring_boot_react_demo.model.dto.response.LyricResponse;
import com.example.spring_boot_react_demo.model.entity.Lyric;
import com.example.spring_boot_react_demo.model.entity.Project;
import com.example.spring_boot_react_demo.model.entity.Video;
import com.example.spring_boot_react_demo.repository.LyricRepo;
import com.example.spring_boot_react_demo.repository.ProjectRepo;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.FFmpegService;
import com.example.spring_boot_react_demo.service.LyricService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.example.spring_boot_react_demo.util.AssUtil.*;
import static com.example.spring_boot_react_demo.util.ConvertUtils.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    ProjectRepo projectRepository;

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
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        Lyric lyric = project.getLyric();
        if (project.isEffect()) {
            lyric.setEffectText(newLyric);
            lyric.setOriginalText(convertEffectTextToOriginal(project.getVideo(), newLyric, project.getDuration()));
        } else {
            lyric.setOriginalText(newLyric);
        }
        lyric.setText(newLyric.trim());
        lyricRepository.save(lyric);

        return ffmpegService.addAssToVideo(file, createAssFile(lyric.getText()));
    }

    @Override
    public LyricResponse showAndHideLyrics(Long projectId, MultipartFile file) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        Lyric lyric = project.getLyric();
        boolean newHiddenState = !lyric.isLyricHidden();
        if (newHiddenState) {
            lyric.setText("");
        } else {
            if (project.isEffect()) lyric.setText(lyric.getEffectText());
            else lyric.setText(lyric.getOriginalText());
        }
        lyric.setLyricHidden(newHiddenState);
        lyricRepository.save(lyric);
        String videoUrl = updateLyricForHiddenLyrics(projectId, lyric.getText(), file);
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
    public MultipartFile applyKaraEffect(MultipartFile videoFile, Long projectId) {
        Lyric lyric = lyricRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Lyric not found for projectId: " + projectId));

        lyric.setText(modifyASSContent(lyric.getText()));
        lyricRepository.save(lyric);
        return ffmpegService.addAssToVideo(videoFile, createAssFile(lyric.getText()));
    }

    @Override
    public String cutLyricsByTimeRange(TreeMap<Integer, Video> videosMap, String text, Double duration) {
        List<Integer> keys = new ArrayList<>(videosMap.keySet());
        String result = text;
        for (int i = 1; i < keys.size(); i++) {
            double checkTime = videosMap.get(i - 1).getEndTime() - (duration * (i-1));
            result = convertOriginalToEffectText(result, checkTime, duration);
        }
        return result;
    }

    @Override
    public String convertEffectTextToOriginal(List<Video> videoList, String effectText, Double duration) {
        TreeMap<Integer, Video> videosMap = convertListVideoToMap(videoList);
        List<Integer> keys = new ArrayList<>(videosMap.keySet());
        String result = effectText;
        for (int i = 1; i < keys.size(); i++) {
            double resetBaseTime = videosMap.get(i - 1).getEndTime() - duration;
            result = convertEffectTextToOriginal(result, resetBaseTime, duration);
        }
        return result;
    }

    private String convertEffectTextToOriginal(String effectText, Double checkTime, Double duration) {
        List<LyricSegment> lyricSegments = convertAssTextToList(effectText);
        List<LyricSegment> result = new ArrayList<>();
        for (LyricSegment lyric : lyricSegments) {
            if (lyric.getStartTime() > checkTime) {
                lyric.setStartTime(lyric.getStartTime() + duration);
                lyric.setEndTime(lyric.getEndTime() + duration);
            }
            result.add(lyric);
        }

        return convertListToAssText(result, effectText);
    }

    private String convertOriginalToEffectText(String text, Double checkTime, Double duration) {
        List<LyricSegment> lyricSegments = convertAssTextToList(text);
        List<LyricSegment> result = new ArrayList<>();
        for (LyricSegment lyric : lyricSegments) {
            double startTime = lyric.getStartTime();
            double endTime = lyric.getEndTime();
            if (endTime < checkTime) {
                result.add(lyric);
            } else {
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
            return cloudinaryService.uploadFile(processedVideo, "video");

        } catch (IOException e) {
            return "Lyric updated, but failed to create video with subtitles";
        }
    }
}