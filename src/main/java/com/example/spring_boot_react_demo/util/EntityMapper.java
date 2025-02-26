package com.example.spring_boot_react_demo.util;

import com.example.spring_boot_react_demo.model.dto.response.BackgroundResponse;
import com.example.spring_boot_react_demo.model.dto.response.CreateProjectResponse;
import com.example.spring_boot_react_demo.model.dto.response.ProjectResponse;
import com.example.spring_boot_react_demo.model.entity.Background;
import com.example.spring_boot_react_demo.model.entity.Project;

import java.util.List;

public class EntityMapper {
    public static CreateProjectResponse mapToCreateProjectResponse(Project project) {
        return new CreateProjectResponse(
                project.getId(),
                project.getName(),
                project.getUploadTime()
        );
    }
    public static ProjectResponse mapToProjectResponse(Project project, List<BackgroundResponse> backgroundResponses) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getUploadTime(),
                project.getLength(),
                backgroundResponses
        );
    }
    public static BackgroundResponse maptoBackgroundResponse (Background background){
        return new BackgroundResponse(
                background.getId(),
                background.getProject().getId(),
                background.getAsset(),
                background.getType(),
                background.getUploadTime(),
                background.getDisplayOrder(),
                background.getStartTime(),
                background.getEndTime()
        );
    }
}
