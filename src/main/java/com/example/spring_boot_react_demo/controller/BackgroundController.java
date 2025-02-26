package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.model.dto.response.ApiResponse;
import com.example.spring_boot_react_demo.model.dto.response.BackgroundResponse;
import com.example.spring_boot_react_demo.service.BackgroundService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/background")
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class BackgroundController {
    @Autowired
    private final BackgroundService backgroundService;

    @PostMapping()
    public ApiResponse<List<BackgroundResponse>> addBackground(@RequestParam List<MultipartFile> files, @RequestParam Long projectId) {
        return ApiResponse.<List<BackgroundResponse>>builder()
                .result(backgroundService.addBackground( files, projectId))
                .build();
    }

    @DeleteMapping()
    public ApiResponse<String> deleteBackground(@RequestParam Long backgroundId) {
        return ApiResponse.<String>builder()
                .result(backgroundService.deleteBackground(backgroundId))
                .build();
    }
}