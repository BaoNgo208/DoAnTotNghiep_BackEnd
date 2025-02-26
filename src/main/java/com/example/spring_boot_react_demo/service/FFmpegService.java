package com.example.spring_boot_react_demo.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FFmpegService {
    public String extractAudio(String videoPath);
    public String cutAudio(MultipartFile file,String startTime,String endTime);
    public String mergeAudio(MultipartFile file1 ,MultipartFile file2);
    public String cutMedia(MultipartFile file,String startTime,String endTime,String fileExtension);
    public String mergeMedia(List<MultipartFile> files,String fileExtension);
    public String convertAndUploadVideo(String videoPath, String outputFormat);
    public String mixAudioVideo(String videoFile,String audioFile,String outputFile);
    public String addSrtToVideo(MultipartFile videoFile, MultipartFile srtFile);
}