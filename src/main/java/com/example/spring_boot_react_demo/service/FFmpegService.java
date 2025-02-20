package com.example.spring_boot_react_demo.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FFmpegService {
    public String extractAudio(String videoPath);
    public String cutAudio(MultipartFile file,String startTime,String endTime);
    public String mergeAudio(MultipartFile file1 ,MultipartFile file2);
    public String cutMedia(MultipartFile file,String startTime,String endTime,String fileExtension);
    public String mergeMedia(List<MultipartFile> files,String fileExtension);

}
