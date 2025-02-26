package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.model.entity.Background;
import com.example.spring_boot_react_demo.model.entity.Project;
import com.example.spring_boot_react_demo.repository.BackgroundRepo;
import com.example.spring_boot_react_demo.service.BackgroundService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BackgroundServiceImpl implements BackgroundService {

    @Autowired
    BackgroundRepo backgroundRepo;

    @Override
    public Background createBackground(Project project, String asset) {
        Background background = new Background();
        background.setAsset(asset);
        background.setProject(project);
        backgroundRepo.save(background);
        return null;
    }
}