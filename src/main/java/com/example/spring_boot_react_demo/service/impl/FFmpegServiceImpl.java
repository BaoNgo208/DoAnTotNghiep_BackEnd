package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.enums.FFmpegTransition;
import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.service.FFmpegService;
import static com.example.spring_boot_react_demo.util.ConvertUtils.*;
import static com.example.spring_boot_react_demo.util.FileUtil.*;
import static com.example.spring_boot_react_demo.util.Constants.*;

import com.example.spring_boot_react_demo.util.Constants;
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
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
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
        }catch (IOException | InterruptedException e){
            e.printStackTrace();
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
        } catch (IOException  e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void applyTransition(String video1, String video2, String outputPath, FFmpegTransition transition, Double duration, String size) {
        Double video1Duration = getVideoDuration(video1);
        Double fadeOutStartTime = video1Duration - duration;
        try {
            runFFmpegCommand(Arrays.asList(
                    "ffmpeg", "-y",
                    "-i", video1,
                    "-i", video2,
                    "-filter_complex",
                    "[0:v]fps=30,scale=" + size.replace("x", ":") + ",settb=AVTB,format=yuv420p[v0];"
                            + "[1:v]fps=30,scale=" + size.replace("x", ":") + ",settb=AVTB,format=yuv420p,tpad=start_duration=" + duration
                            + ",fade=t=in:st=" + duration + ":d=" + duration + "[v1];"
                            + "[v0][v1]xfade=transition=" + transition.getTransitionName() + ":duration=" + duration + ":offset=" + fadeOutStartTime + "[v];"
                            + "[0:a]afade=t=out:st=" + fadeOutStartTime + ":d=" + duration + "[a1];"
                            + "[1:a]adelay=" + (video1Duration * 1000) + "|" + (video1Duration * 1000) + ",afade=t=in:st=0:d=" + duration + "[a2];"
                            + "[a1][a2]amix=inputs=2[a]",
                    "-map", "[v]", "-map", "[a]", "-c:v", "libx264", "-pix_fmt", "yuv420p",
                    "-preset", "ultrafast", "-crf", "23", "-c:a", "aac", "-b:a", "128k",
                    outputPath
            ));
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
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

    public Double getVideoDuration(String videoPath) {
        try {
            String durationStr = runFFmpegCommand(Arrays.asList(
                    "ffprobe",
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    videoPath
            ));
            return Double.parseDouble(durationStr);
        } catch (IOException | InterruptedException e) {
            throw new AppException(ErrorCode.FFMPEG_GET_DURATION_VIDEO_FAIL);
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
            log.info(output.toString());
            throw new RuntimeException("FFmpeg execution failed with exit code: " + exitCode );
        }
        return output.toString();
    }
}