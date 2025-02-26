package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.dto.request.CreateProjectRequest;
import com.example.spring_boot_react_demo.model.dto.request.UpdateProjectRequest;
import com.example.spring_boot_react_demo.model.dto.response.CreateProjectResponse;
import com.example.spring_boot_react_demo.model.dto.response.ProjectResponse;

public interface ProjectService {
    CreateProjectResponse createProject(CreateProjectRequest createProjectRequest);
    ProjectResponse getProject(Long projectId);
    String updateProject(UpdateProjectRequest updateProjectRequest);
}