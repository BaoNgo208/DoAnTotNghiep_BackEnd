package com.example.spring_boot_react_demo.service;

import com.example.spring_boot_react_demo.enums.FFmpegTransition;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FFmpegService {
    MultipartFile addAssToVideo(MultipartFile videoFile, File assFile);
    String convertVideo(String inputVideoPath, String outputFileExtension);
    void applyTransition(String prevVideo, String nextVideo, String outputPath, FFmpegTransition transition, Double duration, Double fadeOutStartTime, String size);
    void mergeVideo(String prevVideo, String nextVideo, String outputPath, String size);
    void mixAudioWithVideo(Path videoPath, Path audioPath, Path outputPath) throws IOException, InterruptedException;
    void mixAudiosWithTiming(Path videoPath, List<Path> audioPaths, List<Double> startTimesInMs, Path outputPath) throws IOException, InterruptedException;
    MultipartFile  applyVintageEffect(String videoUrl ) throws IOException, InterruptedException ;
    MultipartFile applyVintageEffectWithOverlay(String videoUrl,String overlayUrl) throws IOException, InterruptedException;
    MultipartFile applyRetroCameraEffect(String videoUrl,String overlayUrl) throws IOException, InterruptedException ;
    MultipartFile applyRetroCameraEffectWithVintage(String videoUrl,String overlayUrl) throws IOException, InterruptedException ;
    MultipartFile applyNaturalFallEffect(String videoUrl,String overlayUrl,String fallType) throws IOException, InterruptedException ;


}
