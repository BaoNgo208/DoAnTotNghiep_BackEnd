package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.enums.FFmpegTransition;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;

public interface FFmpegService {
    MultipartFile addAssToVideo(MultipartFile videoFile, File assFile);
    String convertVideo(String inputVideoPath, String outputFileExtension);
    void applyTransition(String video1, String video2, String outputPath, FFmpegTransition transition, Double duration, String size);
}
