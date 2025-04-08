package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.enums.FFmpegTransition;
import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.model.MediaType;
import com.example.spring_boot_react_demo.model.dto.request.ApplyTransitionRequest;
import com.example.spring_boot_react_demo.model.dto.request.VideoRequest;
import com.example.spring_boot_react_demo.model.dto.response.VideoResponse;
import com.example.spring_boot_react_demo.model.entity.Lyric;
import com.example.spring_boot_react_demo.model.entity.Project;
import com.example.spring_boot_react_demo.model.entity.Video;
import com.example.spring_boot_react_demo.repository.LyricRepo;
import com.example.spring_boot_react_demo.repository.ProjectRepo;
import com.example.spring_boot_react_demo.repository.VideoRepo;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.FFmpegService;
import com.example.spring_boot_react_demo.service.LyricService;
import com.example.spring_boot_react_demo.service.VideoService;

import static com.example.spring_boot_react_demo.util.AssUtil.createAssFile;
import static com.example.spring_boot_react_demo.util.Constants.*;
import static com.example.spring_boot_react_demo.util.ConvertUtils.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.spring_boot_react_demo.util.EntityMapper.maptoVideoResponse;
import static com.example.spring_boot_react_demo.util.FileUtil.deleteFileIfExists;
import static com.example.spring_boot_react_demo.util.FileUtil.getFileType;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoServiceImpl implements VideoService {

    ProjectRepo projectRepo;
    VideoRepo videoRepo;
    CloudinaryService cloudinaryService;
    FFmpegService ffmpegService;
    LyricService lyricService;
    private final LyricRepo lyricRepo;

    @Override
    public List<VideoResponse> addVideo(List<MultipartFile> files, Long projectId) {
        List<VideoResponse> assets = new ArrayList<>();
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        for (MultipartFile file : files) {
            assets.add(addVideo(file, project));
        }
        return assets;
    }

    @Override
    public String deleteVideo(Long id) {
        Video video = videoRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_FOUND));
        cloudinaryService.deleteFile(video.getAsset(), MediaType.VIDEO.getname());
        videoRepo.delete(video);
        return "Success";
    }

    @Override
    public void updateVideo(VideoRequest videoRequest) {
        Video video = videoRepo.findById(videoRequest.getId())
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_FOUND));
        video.setAsset(videoRequest.getAsset());
        video.setStartTime(videoRequest.getStartTime());
        video.setEndTime(videoRequest.getEndTime());
        videoRepo.save(video);
    }

    @Override
    public MultipartFile applyTransition (ApplyTransitionRequest applyTransitionRequest) throws IOException {
        Project project = projectRepo.findById(applyTransitionRequest.getProjectId())
                .orElseThrow(()-> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        if(project.getBackground() != null || project.getVideo().size() == ONE) {
            throw new AppException(ErrorCode.CANNOT_APPLY_TRANSITION);
        }
        TreeMap<Integer, Video> videosMap = convertListVideoToMap(project.getVideo());

        String outputPath = processVideosWithTransitions(videosMap, applyTransitionRequest, project.getSize());
        MultipartFile outputMultipal = convertFileToMultipartFile(new File(outputPath));
        if(project.getLyric() != null) {
            Lyric lyric = project.getLyric();
            String newLyric ;
            if(lyric.getOriginalText() != null && !lyric.isLyricHidden()) {
                newLyric = lyricService.cutLyricsByTimeRange(videosMap, lyric.getOriginalText(), applyTransitionRequest.getDuration());
            }else {
                newLyric = lyricService.cutLyricsByTimeRange(videosMap, lyric.getText(), applyTransitionRequest.getDuration());
                lyric.setOriginalText(lyric.getText());
            }
            lyric.setText(newLyric);
            lyricRepo.save(lyric);
            outputMultipal = ffmpegService.addAssToVideo(outputMultipal, createAssFile(newLyric));
        }

        deleteFileIfExists(outputPath);
        return outputMultipal;
    }

    private String processVideosWithTransitions(TreeMap<Integer, Video> videosMap, ApplyTransitionRequest applyTransitionRequest, String size) {
        FFmpegTransition ffmpegTransition = FFmpegTransition.fromString(applyTransitionRequest.getTransitionType());

        List<Integer> keys = new ArrayList<>(videosMap.keySet());
        String prevVideoPath = videosMap.get(keys.get(0)).getAsset();
        String outputPath = null;
        for (int i = 1 ; i < keys.size() ; i++) {
            String nextVideoPath = videosMap.get(keys.get(i)).getAsset();
            outputPath = OUTPUT_VIDEO_FILE + i + MP4;
            ffmpegService.applyTransition(prevVideoPath, nextVideoPath, outputPath, ffmpegTransition, applyTransitionRequest.getDuration(), size);
            prevVideoPath = outputPath;
            deleteFileIfExists(OUTPUT_VIDEO_FILE + (i-1) + MP4);
        }
        assert outputPath != null;
        return outputPath;
    }

    public VideoResponse addVideo(MultipartFile file, Project project) {
        String filetype = getFileType(file);
        if (!filetype.equals(MediaType.VIDEO.getname())) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FORMAT);
        }
        Video video = new Video();
        video.setAsset(cloudinaryService.uploadFile(file, filetype));
        video.setProject(project);
        video.setUploadTime(LocalDateTime.now());
        return maptoVideoResponse(videoRepo.save(video));
    }

    private TreeMap<Integer, Video> convertListVideoToMap(List<Video> videoList) {
        return videoList.stream()
                .sorted(Comparator.comparingDouble(Video::getStartTime))
                .collect(Collectors.toMap(
                        videoList::indexOf,
                        v -> v,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }
}
