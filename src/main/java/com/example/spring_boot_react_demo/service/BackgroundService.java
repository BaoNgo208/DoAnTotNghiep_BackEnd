package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.entity.Background;
import com.example.spring_boot_react_demo.model.entity.Project;

public interface BackgroundService {
    Background createBackground(Project project, String asset);
}