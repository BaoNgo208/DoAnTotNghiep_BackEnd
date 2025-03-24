package com.example.spring_boot_react_demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FFmpegService {
    MultipartFile addAssToVideo(MultipartFile videoFile, File assFile);
    String convertVideo(String inputVideoPath, String outputFileExtension);
}
