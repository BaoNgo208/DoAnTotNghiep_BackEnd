package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.model.MediaType;
import com.example.spring_boot_react_demo.model.dto.request.VideoRequest;
import com.example.spring_boot_react_demo.model.dto.response.VideoResponse;
import com.example.spring_boot_react_demo.model.entity.Video;
import com.example.spring_boot_react_demo.repository.ProjectRepo;
import com.example.spring_boot_react_demo.repository.VideoRepo;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.VideoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static com.example.spring_boot_react_demo.util.EntityMapper.maptoVideoResponse;
import static com.example.spring_boot_react_demo.util.FileUtil.getFileType;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoServiceImpl implements VideoService {

    @Autowired
    ProjectRepo projectRepo;

    @Autowired
    VideoRepo videoRepo;

    @Autowired
    CloudinaryService cloudinaryService;

    @Override
    public List<VideoResponse> addVideo(List<MultipartFile> files, Long projectId) {
        List<VideoResponse> assets = new ArrayList<>();
        for (MultipartFile file : files) {
            assets.add(addVideo(file, projectId));
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

    public VideoResponse addVideo(MultipartFile file, Long projectId) {
        String filetype = getFileType(file);
        log.info("VIDEO: {}", MediaType.VIDEO.getname());
        if (!filetype.equals(MediaType.VIDEO.getname())) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FORMAT);
        }
        Video video = new Video();
        video.setAsset(cloudinaryService.uploadFile(file, "folder_1",filetype));
        video.setProject(projectRepo.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND)));
        video.setUploadTime(LocalDateTime.now());
        return maptoVideoResponse(videoRepo.save(video));
    }
}
