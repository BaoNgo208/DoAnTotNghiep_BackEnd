package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.service.FFmpegService;
import static com.example.spring_boot_react_demo.util.ConvertUtils.*;
import static com.example.spring_boot_react_demo.util.FileUtil.*;
import static com.example.spring_boot_react_demo.util.Constants.*;
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
            File tempVideoFile = convertMultipartFileToFile(videoFile, TEMP_VIDEO_FILE);
            String outputPath = OUTPUT_VIDEO_FILE;

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
            throw new AppException(ErrorCode.FFMPEG_CREATE_VIDEO_FAIL);
        }
    }

    private void runFFmpegCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg execution failed with exit code: " + exitCode);
        }
    }
}