package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.dto.request.AddVideosRequest;
import com.example.spring_boot_react_demo.model.dto.request.ApplyTransitionRequest;
import com.example.spring_boot_react_demo.model.dto.request.VideoRequest;
import com.example.spring_boot_react_demo.model.dto.response.AddAudioToVideoResponse;
import com.example.spring_boot_react_demo.model.dto.response.AddBackgroundResponse;
import com.example.spring_boot_react_demo.model.dto.response.VideoResponse;
import com.example.spring_boot_react_demo.model.entity.Video;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface VideoService {
    List<VideoResponse> addVideo(AddVideosRequest addVideosRequest);
    String deleteVideo(Long id);
    void updateVideo(VideoRequest backgroundRequest);
    MultipartFile applyTransition (ApplyTransitionRequest applyTransitionRequest) throws IOException;
    MultipartFile removeEffect(Long projectId) throws IOException;
    AddBackgroundResponse addBackground(String videoPath, String backgroundPath, Long videoId) throws IOException;
    AddBackgroundResponse removeBackground(String videoPath, Long videoId);
    AddAudioToVideoResponse addAudioToVideo(MultipartFile video,MultipartFile audio);
    Video getVideoById(Long id);
    void saveVideo(Video newVideo);

    String applyVintageEffect(Long videoId) throws IOException, InterruptedException;
    String applyVintageEffectWithOverlay(Long videoId,String overlayPath) throws IOException, InterruptedException;

}
