package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.model.File;
import com.example.spring_boot_react_demo.model.dto.request.BackgroundRequest;
import com.example.spring_boot_react_demo.model.dto.response.BackgroundResponse;
import com.example.spring_boot_react_demo.model.entity.Background;
import com.example.spring_boot_react_demo.repository.BackgroundRepo;
import com.example.spring_boot_react_demo.repository.ProjectRepo;
import com.example.spring_boot_react_demo.service.BackgroundService;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.example.spring_boot_react_demo.util.FileUtil.*;
import static com.example.spring_boot_react_demo.util.EntityMapper.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class BackgroundServiceImpl implements BackgroundService {

    @Autowired
    private final ProjectRepo projectRepo;
    @Autowired
    private final BackgroundRepo backgroundRepo;
    @Autowired
    private final CloudinaryService cloudinaryService;

    @Override
    public List<BackgroundResponse> addBackground(List<MultipartFile> files, Long projectId) {
        List<BackgroundResponse> assets = new ArrayList<>();
        for (MultipartFile file : files) {
            assets.add(addBackground(file, projectId));
        }
        return assets;
    }

    @Override
    public String deleteBackground(Long id) {
        Background background = backgroundRepo.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.BACKGROUND_NOT_FOUND));
        cloudinaryService.deleteFile(background.getAsset(),background.getType());
        backgroundRepo.delete(background);
        return "Success";
    }

    @Override
    public Background updateBackground(BackgroundRequest backgroundRequest) {
        Background background = backgroundRepo.findById(backgroundRequest.getId())
                .orElseThrow(()->new AppException(ErrorCode.BACKGROUND_NOT_FOUND));
        background.setAsset(backgroundRequest.getAsset());
        background.setDisplayOrder(backgroundRequest.getDisplayOrder());
        background.setStartTime(backgroundRequest.getStartTime());
        background.setEndTime(backgroundRequest.getEndTime());

        return backgroundRepo.save(background);
    }

    public BackgroundResponse addBackground(MultipartFile file, Long projectId){
        String filetype = getFileType(file);
        if (!filetype.equals(File.VIDEO.name()) && !filetype.equals(File.IMAGE.name()) ) {
            throw new AppException(ErrorCode.INVALID_VIDEO_FORMAT);
        }
        Background background = new Background();
        background.setAsset(cloudinaryService.uploadFile(file,"folder_1", filetype));
        background.setType(filetype);
        background.setProject(projectRepo.findById(projectId)
                .orElseThrow(()-> new AppException(ErrorCode.PROJECT_NOT_FOUND)));
        background.setUploadTime(LocalDateTime.now());

        return  maptoBackgroundResponse(backgroundRepo.save(background));
    }
}