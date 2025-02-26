package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.service.FFmpegService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AudioController {
    private final FFmpegService ffmpegService;

    @PostMapping("/cut-audio")
    public String cutAudio(@RequestParam("file") MultipartFile file,
                           @RequestParam("start") String startTime,
                           @RequestParam("end") String endTime) {
        return  ffmpegService.cutAudio(file,startTime,endTime);
    }

    @PostMapping("/merge-audio")
    public String mergeAudio(@RequestParam("file1") MultipartFile file1,
                             @RequestParam("file2") MultipartFile file2) {
      return ffmpegService.mergeAudio(file1,file2);
    }
    @PostMapping("/cut-media")
    public String cutMedia(@RequestParam("file") MultipartFile file,
                           @RequestParam("start") String startTime,
                           @RequestParam("end") String endTime) {

        String fileName= file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        return  ffmpegService.cutMedia(file,startTime,endTime,fileExtension);
    }
    @PostMapping("/merge-media")
    public String mergeMedia(@RequestParam("files") List<MultipartFile> fileList) {
        String firstFile = fileList.get(0).getOriginalFilename();
        String fileExtension  = firstFile.substring(firstFile.lastIndexOf("."));
        return ffmpegService.mergeMedia(fileList,fileExtension);
    }
    @PostMapping("/mix-audio-video")
    public String mixAudioVideo(@RequestParam("videoFile") String videoFile,
                                @RequestParam("audioFile") String audioFile,
                                @RequestParam("outputFile") String outputFile) {
        return ffmpegService.mixAudioVideo(videoFile, audioFile,outputFile);
    }
}