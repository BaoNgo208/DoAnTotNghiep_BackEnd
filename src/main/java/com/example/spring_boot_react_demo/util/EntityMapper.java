package com.example.spring_boot_react_demo.util;

import com.example.spring_boot_react_demo.model.dto.response.ProjectBasicResponse;
import com.example.spring_boot_react_demo.model.dto.response.ProjectFullResponse;
import com.example.spring_boot_react_demo.model.dto.response.VideoResponse;
import com.example.spring_boot_react_demo.model.entity.Project;
import com.example.spring_boot_react_demo.model.entity.Video;
import java.util.List;

public class EntityMapper {
    public static ProjectBasicResponse mapToProjectBasicResponse(Project project) {
        return new ProjectBasicResponse(
                project.getId(),
                project.getName(),
                project.getUploadTime(),
                project.getLength(),
                project.getAsset(),
                project.getSize(),
                project.getBackground()
        );
    }
    public static ProjectFullResponse mapToProjectResponse(Project project, List<VideoResponse> videoResponses) {
        return new ProjectFullResponse(
                project.getId(),
                project.getName(),
                project.getUploadTime(),
                project.getLength(),
                project.getSize(),
                project.getAsset(),
                videoResponses,
                project.getBackground(),
                project.getLyric()
        );
    }
    public static VideoResponse maptoVideoResponse (Video video){
        return new VideoResponse(
                video.getId(),
                video.getProject().getId(),
                video.getAsset(),
                video.getUploadTime(),
                video.getStartTime(),
                video.getEndTime()
        );
    }
}