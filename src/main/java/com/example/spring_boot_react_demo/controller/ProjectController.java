package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.model.dto.request.CreateProjectRequest;
import com.example.spring_boot_react_demo.model.dto.request.UpdateProjectRequest;
import com.example.spring_boot_react_demo.model.dto.response.ApiResponse;
import com.example.spring_boot_react_demo.model.dto.response.ProjectBasicResponse;
import com.example.spring_boot_react_demo.model.dto.response.ProjectFullResponse;
import com.example.spring_boot_react_demo.service.ProjectService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.example.spring_boot_react_demo.util.FileUtil.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/project")
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {
    @Autowired
    ProjectService projectService;

    @PostMapping("/createProject")
    public ApiResponse<ProjectBasicResponse> createProject(@RequestBody CreateProjectRequest createProjectRequest) {
        return ApiResponse.<ProjectBasicResponse>builder()
                .result(projectService.createProject(createProjectRequest))
                .build();
    }
    @GetMapping
    public ApiResponse<ProjectFullResponse> getProjectById(@RequestParam Long id) {
        return ApiResponse.<ProjectFullResponse>builder()
                .result(projectService.getProject(id))
                .build();
    }
    @PatchMapping("/updateProject")
    public ApiResponse<String> UpdateProject(@RequestBody UpdateProjectRequest updateProjectRequest) {
        return ApiResponse.<String>builder()
                .result(projectService.updateProject(updateProjectRequest))
                .build();
    }
    @GetMapping("/exportProject")
    public ResponseEntity<Resource> exportProject(@RequestParam Long projectId,
                                                  @RequestParam String outputVideoPath) {
        Resource resource = projectService.exportProject(projectId, outputVideoPath);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } finally {
            deleteFileIfExists(resource.getFilename());
        }
    }
    @PatchMapping("/updateProjectAsset")
    public ApiResponse<Void> updateProjectAsset(@RequestParam Long projectId, @RequestParam String updatedUrl) throws IOException {
        projectService.updateProjectAsset(projectId, updatedUrl);
        return ApiResponse.<Void>builder()
                .message("Update successfully")
                .build();
    }
}