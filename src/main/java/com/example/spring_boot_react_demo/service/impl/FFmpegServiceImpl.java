package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.enums.FFmpegTransition;
import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.service.FFmpegService;

import static com.example.spring_boot_react_demo.util.ConvertUtils.*;
import static com.example.spring_boot_react_demo.util.FileUtil.*;
import static com.example.spring_boot_react_demo.util.Constants.*;
import static com.example.spring_boot_react_demo.util.CloudinaryUtil.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FFmpegServiceImpl implements FFmpegService {

    @Override
    public String convertVideo(String inputVideoPath, String outputFileExtension) {
        String outputVideoPath = "output" + outputFileExtension;
        try {
            runFFmpegCommand(Arrays.asList(
                    "ffmpeg", "-y",
                    "-i", inputVideoPath,
                    outputVideoPath
            ));
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return outputVideoPath;
    }

    @Override
    public MultipartFile addAssToVideo(MultipartFile videoFile, File assFile) {
        try {
            File tempVideoFile = convertMultipartFileToFile(videoFile, TEMP_VIDEO_FILE + MP4);
            String outputPath = OUTPUT_VIDEO_FILE + MP4;

            addAssToVideo(tempVideoFile.getAbsolutePath(),
                    assFile,
                    outputPath);

            MultipartFile mergedFile = convertFileToMultipartFile(new File(outputPath));
            deleteFileIfExists(assFile.getAbsolutePath());
            deleteFileIfExists(tempVideoFile.getAbsolutePath());
            deleteFileIfExists(outputPath);
            return mergedFile;
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public void applyTransition(String prevVideo, String nextVideo, String outputPath, FFmpegTransition transition, Double duration, Double fadeOutStartTime, String size) {
        try {
            runFFmpegCommand(Arrays.asList(
                    "ffmpeg", "-y",
                    "-i", addSizeForUrl(prevVideo, size),
                    "-i", addSizeForUrl(nextVideo, size),
                    "-filter_complex",
                    "[0:v]fps=30,settb=AVTB,format=yuv420p[v0];"
                            + "[1:v]fps=30,settb=AVTB,format=yuv420p[v1];"
                            + "[v0][v1]xfade=transition=" + transition.getTransitionName() + ":duration=" + duration + ":offset=" + fadeOutStartTime + "[v];"
                            + "[0:a]afade=t=out:st=" + fadeOutStartTime + ":d=" + duration + "[a1];"
                            + "[1:a]adelay=" + (fadeOutStartTime * 1000) + "|" + (fadeOutStartTime * 1000) + ",afade=t=in:st=0:d=" + duration + "[a2];"
                            + "[a1][a2]amix=inputs=2[a]",
                    "-map", "[v]", "-map", "[a]", "-c:v", "libx264", "-pix_fmt", "yuv420p",
                    "-preset", "ultrafast", "-crf", "23", "-c:a", "aac", "-b:a", "128k",
                    outputPath
            ));
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void mergeVideo(String prevVideo, String nextVideo, String outputPath, String size) {
        try {
            runFFmpegCommand(Arrays.asList(
                    "ffmpeg",
                    "-i", addSizeForUrl(prevVideo, size),
                    "-i", addSizeForUrl(nextVideo, size),
                    "-filter_complex", "[0:v:0] [0:a:0] [1:v:0] [1:a:0] concat=n=2:v=1:a=1 [v] [a]",
                    "-map", "[v]",
                    "-map", "[a]",
                    outputPath
            ));
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private void addAssToVideo(String videoPath, File assFile, String outputPath) {
        try {
            String escapedSubtitlePath = assFile.getAbsolutePath().replace("\\", "\\\\").replace(":", "\\:");
            runFFmpegCommand(Arrays.asList(
                    "ffmpeg", "-y",
                    "-i", videoPath,
                    "-vf", "subtitles='" + escapedSubtitlePath + "'",
                    "-c:v", "libx264", "-crf", "23", "-preset", "fast",
                    "-c:a", "copy", outputPath
            ));
        } catch (IOException | InterruptedException e) {
            throw new AppException(ErrorCode.FFMPEG_ADD_LYRIC_FAIL);
        }
    }

    private String runFFmpegCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(NEW_LINE);
            }
        }
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.error(output.toString());
            throw new RuntimeException("FFmpeg execution failed with exit code: " + exitCode);
        }
        return output.toString();
    }
}