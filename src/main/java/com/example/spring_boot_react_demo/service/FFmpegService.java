package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.model.entity.Project;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;

public interface FFmpegService {
    MultipartFile addAssToVideo(MultipartFile videoFile, File assFile);
    String createFullVideo(Project project, String outputVideoPath);
    public String cutAudio(MultipartFile file,String startTime,String endTime);
    public String mergeAudio(MultipartFile file1 ,MultipartFile file2);
    public String cutMedia(MultipartFile file,String startTime,String endTime,String fileExtension);
    public String mergeMedia(List<MultipartFile> files,String fileExtension);
    public String mixAudioVideo(String videoFile,String audioFile,String outputFile);
}
