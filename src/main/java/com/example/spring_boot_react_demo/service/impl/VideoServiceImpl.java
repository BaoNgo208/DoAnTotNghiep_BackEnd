package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.enums.FFmpegTransition;
import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.model.MediaType;
import com.example.spring_boot_react_demo.model.dto.request.AddVideosRequest;
import com.example.spring_boot_react_demo.model.dto.request.ApplyTransitionRequest;
import com.example.spring_boot_react_demo.model.dto.request.VideoRequest;
import com.example.spring_boot_react_demo.model.dto.response.AddAudioToVideoResponse;
import com.example.spring_boot_react_demo.model.dto.response.AddBackgroundResponse;
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
import static com.example.spring_boot_react_demo.util.EntityMapper.mapToAddBackgroundResponse;
import static com.example.spring_boot_react_demo.util.EntityMapper.maptoVideoResponse;
import static com.example.spring_boot_react_demo.util.FileUtil.*;
import static com.example.spring_boot_react_demo.util.CloudinaryUtil.*;

import com.example.spring_boot_react_demo.util.CloudinaryUtil;
import jakarta.persistence.EntityNotFoundException;
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
    LyricRepo lyricRepo;

    @Override
    public List<VideoResponse> addVideo(AddVideosRequest addVideosRequest) {
        List<VideoResponse> assets = new ArrayList<>();
        Project project = projectRepo.findById(addVideosRequest.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        for (VideoRequest video : addVideosRequest.getVideoRequestList()) {
            assets.add(addVideo(video, project));
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

        video.setStartTime(videoRequest.getStartTime());
        String videoVersion = getVersionOfVideo(video.getAsset());
        video.setAsset(concatVersionWithVideoAsset(videoRequest.getAsset(), videoVersion));
        video.setVideoWithBackground(videoRequest.getAssetWithBackground());
        video.setEndTime(videoRequest.getEndTime());
        videoRepo.save(video);
    }

    @Override
    public MultipartFile applyTransition(ApplyTransitionRequest applyTransitionRequest) throws IOException {
        Project project = projectRepo.findById(applyTransitionRequest.getProjectId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        if (project.getVideo().size() == ONE) {
            throw new AppException(ErrorCode.CANNOT_APPLY_TRANSITION);
        }
        TreeMap<Integer, Video> videosMap = convertListVideoToMap(project.getVideo());
        String outputPath = processVideosWithTransitions(videosMap, applyTransitionRequest, project.getSize());
        MultipartFile outputMultipal = convertFileToMultipartFile(new File(outputPath));

        if (project.getLyric() != null && !project.getLyric().isLyricHidden()) {
            Lyric lyric = project.getLyric();
            String newLyric = lyricService.cutLyricsByTimeRange(videosMap, lyric.getOriginalText(), applyTransitionRequest.getDuration());
            lyric.setText(newLyric);
            lyric.setEffectText(newLyric);
            lyricRepo.save(lyric);
            outputMultipal = ffmpegService.addAssToVideo(outputMultipal, createAssFile(newLyric));
        }

        project.setEffect(true);
        project.setDuration(applyTransitionRequest.getDuration());
        projectRepo.save(project);
        deleteFileIfExists(outputPath);
        return outputMultipal;
    }

    @Override
    public MultipartFile removeEffect(Long projectId) throws IOException {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        if (!project.isEffect()) {
            throw new AppException(ErrorCode.EFFECT_DOES_NOT_EXIT);
        }
        TreeMap<Integer, Video> videosMap = convertListVideoToMap(project.getVideo());
        String outputPath = processVideosWithNoTransitions(videosMap, project.getSize());
        MultipartFile outputMultipal = convertFileToMultipartFile(new File(outputPath));

        Lyric lyric = project.getLyric();
        if (lyric != null && !lyric.isLyricHidden()) {
            lyric.setText(lyric.getOriginalText());
            lyricRepo.save(lyric);
            outputMultipal = ffmpegService.addAssToVideo(outputMultipal, createAssFile(lyric.getText()));
        }
        project.setEffect(false);
        projectRepo.save(project);
        deleteFileIfExists(outputPath);
        return outputMultipal;
    }

    @Override
    public AddBackgroundResponse addBackground(String videoPath, String backgroundPath, Long videoId) throws IOException {
        Video video = videoRepo.findById(videoId).orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_FOUND));
        String videoVersion = getVersionOfVideo(video.getAsset());
        String updatedVideoPath = concatVersionWithVideoAsset(videoPath, videoVersion);
        String backgroundId = getPublicId(backgroundPath);

        String urlWithBackground = addBackgroundForUrl(updatedVideoPath, backgroundId);
        video.setVideoWithBackground(urlWithBackground);

        String newUrl = cloudinaryService.uploadFile(removeTimeFromUrl(urlWithBackground));
        double[] timeRange = extractStartAndEndTime(urlWithBackground);
        video.setAsset(addTimeRangeForUrl(newUrl, timeRange[ZERO], timeRange[ONE]));
        return mapToAddBackgroundResponse(videoRepo.save(video));
    }

    @Override
    public AddBackgroundResponse removeBackground(String videoPath, Long videoId) {
        Video video = videoRepo.findById(videoId)
                .orElseThrow(() -> new AppException(ErrorCode.VIDEO_NOT_FOUND));

        String rawUrl = removeBackgroundForUrl(videoPath);
        String cleanedUrl = cleanCloudinaryUrl(rawUrl);

        video.setAsset(cleanedUrl);
        video.setVideoWithBackground(null);
        return mapToAddBackgroundResponse(videoRepo.save(video));
    }



    private String processVideosWithNoTransitions(TreeMap<Integer, Video> videosMap, String size) {
        List<Integer> keys = new ArrayList<>(videosMap.keySet());
        String prevVideoPath = videosMap.get(keys.get(0)).getAsset();
        String outputPath = null;
        for (int i = 1; i < keys.size(); i++) {
            String nextVideoPath = videosMap.get(keys.get(i)).getAsset();
            outputPath = OUTPUT_VIDEO_FILE + i + MP4;
            ffmpegService.mergeVideo(prevVideoPath, nextVideoPath, outputPath, size);
            prevVideoPath = outputPath;
            deleteFileIfExists(OUTPUT_VIDEO_FILE + (i - 1) + MP4);
        }
        return outputPath;
    }

    private String processVideosWithTransitions(TreeMap<Integer, Video> videosMap, ApplyTransitionRequest applyTransitionRequest, String size) {
        FFmpegTransition ffmpegTransition = FFmpegTransition.fromString(applyTransitionRequest.getTransitionType());
        double duration = applyTransitionRequest.getDuration();
        List<Integer> keys = new ArrayList<>(videosMap.keySet());
        String prevVideoPath = videosMap.get(keys.get(0)).getAsset();
        String outputPath = null;
        for (int i = 1; i < keys.size(); i++) {
            String nextVideoPath = videosMap.get(keys.get(i)).getAsset();
            Double fadeOutStartTime = videosMap.get(keys.get(i - 1)).getEndTime() - (i * duration);
            outputPath = OUTPUT_VIDEO_FILE + i + MP4;
            ffmpegService.applyTransition(prevVideoPath, nextVideoPath, outputPath, ffmpegTransition, duration, fadeOutStartTime, size);
            prevVideoPath = outputPath;
            deleteFileIfExists(OUTPUT_VIDEO_FILE + (i - 1) + MP4);
        }
        assert outputPath != null;
        return outputPath;
    }

    public VideoResponse addVideo(VideoRequest videoRequest, Project project) {
        Video video = new Video();
        String newVideoAsset = addTimeRangeForUrl(videoRequest.getAsset(), ZERO, videoRequest.getDuration());
        video.setAsset(newVideoAsset);
        video.setProject(project);
        video.setUploadTime(LocalDateTime.now());
        return maptoVideoResponse(videoRepo.save(video));
    }


    @Override
    public AddAudioToVideoResponse addAudioToVideo(MultipartFile video, MultipartFile audio) {
        return null;
    }

    @Override
    public Video getVideoById(Long id) {
        return videoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video not found with id: " + id));
    }

    @Override
    public void saveVideo(Video newVideo) {
        videoRepo.save(newVideo);
    }

    @Override
    public String applyVintageEffect(Long videoId) throws IOException, InterruptedException {
        Video video = videoRepo.findById(videoId).orElseThrow(() -> new EntityNotFoundException("cant not found video with id:" + videoId));
        MultipartFile videoAfterAddedEffect = ffmpegService.applyVintageEffect(video.getAsset());
        double [] prevSoAndEo = CloudinaryUtil.extractStartAndEndTime(video.getAsset());
        String newVideoUrl = cloudinaryService.uploadFile(videoAfterAddedEffect,MediaType.VIDEO.getname());
        String newVideoUrlWithSoAndEo = CloudinaryUtil.addTimeRangeForUrl(newVideoUrl,prevSoAndEo[ZERO],prevSoAndEo[ONE]);
        video.setAsset(newVideoUrlWithSoAndEo);
        videoRepo.save(video);
        return newVideoUrlWithSoAndEo;
    }

    @Override
    public String applyVintageEffectWithOverlay(Long videoId,String overlayPath) throws IOException, InterruptedException {
        Video video = videoRepo.findById(videoId).orElseThrow(() -> new EntityNotFoundException("cant not found video with id:" + videoId));
        MultipartFile videoAfterAddedEffect = ffmpegService.applyVintageEffectWithOverlay(video.getAsset(),overlayPath);
        double [] prevSoAndEo = CloudinaryUtil.extractStartAndEndTime(video.getAsset());
        String newVideoUrl = cloudinaryService.uploadFile(videoAfterAddedEffect,MediaType.VIDEO.getname());
        String newVideoUrlWithSoAndEo = CloudinaryUtil.addTimeRangeForUrl(newVideoUrl,prevSoAndEo[ZERO],prevSoAndEo[ONE]);
        video.setAsset(newVideoUrlWithSoAndEo);
        videoRepo.save(video);
        return newVideoUrlWithSoAndEo;
    }
}
