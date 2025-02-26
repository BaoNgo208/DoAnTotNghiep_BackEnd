package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.model.MediaType;
import com.example.spring_boot_react_demo.model.dto.request.CreateProjectRequest;
import com.example.spring_boot_react_demo.model.dto.request.UpdateProjectRequest;
import com.example.spring_boot_react_demo.model.dto.request.VideoRequest;
import com.example.spring_boot_react_demo.model.dto.response.ProjectBasicResponse;
import com.example.spring_boot_react_demo.model.dto.response.ProjectFullResponse;
import com.example.spring_boot_react_demo.model.entity.Background;
import com.example.spring_boot_react_demo.model.entity.Project;
import com.example.spring_boot_react_demo.model.entity.Video;
import com.example.spring_boot_react_demo.repository.ProjectRepo;
import com.example.spring_boot_react_demo.service.*;
import com.example.spring_boot_react_demo.util.EntityMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import static com.example.spring_boot_react_demo.util.EntityMapper.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectServiceImpl implements ProjectService {
    ProjectRepo projectRepo;
    VideoService videoService;
    CloudinaryService cloudinaryService;
    BackgroundService backgroundService;
    FFmpegService ffmpegService;

    @Override
    public ProjectBasicResponse createProject(CreateProjectRequest createProjectRequest) {
        Project project = new Project();
        project.setName(createProjectRequest.getName());
        projectRepo.save(project);
        return mapToProjectBasicResponse(project);
    }

    @Override
    public ProjectFullResponse getProject(Long projectId) {
        Project project = getProjectById(projectId);
        List<Video> video = project.getVideo();
        return mapToProjectResponse(project,
                video.stream()
                        .map(EntityMapper::maptoVideoResponse)
                        .toList());
    }

    @Override
    public String updateProject(UpdateProjectRequest updateProjectRequest) {
        Project project = getProjectById(updateProjectRequest.getId());
        project.setName(updateProjectRequest.getName());
        project.setUploadTime(updateProjectRequest.getUploadTime());
        project.setLength(updateProjectRequest.getLength());
        for (VideoRequest video : updateProjectRequest.getVideos()) {
            videoService.updateVideo(video);
        }
        projectRepo.save(project);
        return "Success";
    }

    @Override
    public String addBackground(Long projectId, MultipartFile backgroundFile) {
        Project project =  getProjectById(projectId);
        String projectBackground = cloudinaryService.uploadFile(backgroundFile,"folder_1", MediaType.IMAGE.getname());
        Background background = backgroundService.createBackground(project, projectBackground);
        project.setBackground(background);
        projectRepo.save(project);
        return "Success";
    }

    private Project getProjectById(Long projectId){
        return projectRepo.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
    }
}