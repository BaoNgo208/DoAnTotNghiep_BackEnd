package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.model.dto.request.CreateProjectRequest;
import com.example.spring_boot_react_demo.model.dto.request.UpdateProjectRequest;
import com.example.spring_boot_react_demo.model.dto.response.ApiResponse;
import com.example.spring_boot_react_demo.model.dto.response.CreateProjectResponse;
import com.example.spring_boot_react_demo.model.dto.response.ProjectResponse;
import com.example.spring_boot_react_demo.service.ProjectService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/project")
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {
    @Autowired
    ProjectService projectService;

    @PostMapping("/createProject")
    public ApiResponse<CreateProjectResponse> createProject(@RequestBody CreateProjectRequest createProjectRequest) {
        return ApiResponse.<CreateProjectResponse>builder()
                .result(projectService.createProject(createProjectRequest))
                .build();
    }

    @GetMapping
    public ApiResponse<ProjectResponse> getProjectById(@RequestParam Long id) {
        return ApiResponse.<ProjectResponse>builder()
                .result(projectService.getProject(id))
                .build();
    }

    @PostMapping("/updateProject")
    public ApiResponse<String> UpdateProject(@RequestBody UpdateProjectRequest updateProjectRequest) {
        return ApiResponse.<String>builder()
                .result(projectService.updateProject(updateProjectRequest))
                .build();
    }
}