package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.dto.request.CreateProjectRequest;
import com.example.spring_boot_react_demo.model.dto.request.UpdateProjectRequest;
import com.example.spring_boot_react_demo.model.dto.response.ProjectBasicResponse;
import com.example.spring_boot_react_demo.model.dto.response.ProjectFullResponse;
import com.example.spring_boot_react_demo.model.entity.Project;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ProjectService {
    ProjectBasicResponse createProject(CreateProjectRequest createProjectRequest);
    ProjectFullResponse getProject(Long projectId);
    String updateProject(UpdateProjectRequest updateProjectRequest);
    Resource exportProject(Long projectId, String outputVideoPath);
    Project getProjectById(Long projectId);
    void saveProject(Project project);
    void updateProjectAsset(Long id,String asset);
}