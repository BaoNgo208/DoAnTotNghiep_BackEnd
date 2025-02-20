package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.entity.ImageModel;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ImageService {
    ResponseEntity<Map<String, String>>  uploadImage(ImageModel imageModel);
}
