package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.model.MediaType;
import com.example.spring_boot_react_demo.model.entity.Project;
import com.example.spring_boot_react_demo.model.entity.Video;
import com.example.spring_boot_react_demo.repository.ProjectRepo;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.FFmpegService;
import static com.example.spring_boot_react_demo.util.Constants.*;
import static com.example.spring_boot_react_demo.util.ConvertUtils.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class FFmpegServiceImpl implements FFmpegService {
    CloudinaryService cloudinaryService;
    ProjectRepo projectRepo;

    @Override
    public String cutAudio(MultipartFile file, String startTime, String endTime) {
        try{
            File tempFile = File.createTempFile("temp_audio_",".mp3");
            file.transferTo(tempFile);

            String outputFilePath = "output_" + tempFile.getName();
            String ffmpegCommand = String.format(
                    "ffmpeg -i %s -ss %s -to %s -c copy %s",
                    tempFile.getAbsolutePath(), startTime, endTime, outputFilePath
            );

            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand.split(" "));
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();

            return "Audio file cut successfully. Output file: " + outputFilePath;
        }catch (IOException | InterruptedException e)  {
            e.printStackTrace();
            return "Error while cutting audio.";
        }
    }
    @Override
    public String mergeAudio(MultipartFile file1, MultipartFile file2) {
        try {
            File tempFile1 = File.createTempFile("temp_audio_1_", ".mp3");
            File tempFile2 = File.createTempFile("temp_audio_2_", ".mp3");
            file1.transferTo(tempFile1);
            file2.transferTo(tempFile2);

            File listFile = File.createTempFile("fileList", ".txt");
            String listContent = "file '" + tempFile1.getAbsolutePath() + "'\n" +
                    "file '" + tempFile2.getAbsolutePath() + "'\n";
            java.nio.file.Files.write(listFile.toPath(), listContent.getBytes());

            String outputFilePath = "output_merged.mp3";
            String ffmpegCommand = String.format(
                    "ffmpeg -f concat -safe 0 -i %s -c copy %s",
                    listFile.getAbsolutePath(), outputFilePath
            );

            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand.split(" "));
            processBuilder.inheritIO();  // Hiển thị output ra console
            Process process = processBuilder.start();
            process.waitFor();

            return "Audio files merged successfully. Output file: " + outputFilePath;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error while merging audio.";
        }
    }
    @Override
    public String cutMedia(MultipartFile file, String startTime, String endTime,String fileExtension) {
        try {
            File tempFile = File.createTempFile("temp_media_", fileExtension);
            file.transferTo(tempFile);

            String outputFilePath = "output_" + tempFile.getName();
            String ffmpegCommand = String.format(
                    "ffmpeg -i %s -ss %s -to %s -c copy %s",
                    tempFile.getAbsolutePath(), startTime, endTime, outputFilePath
            );

            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand.split(" "));
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();

            return "Media file cut successfully. Output file: " + outputFilePath;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error while cutting media.";
        }
    }
    @Override
    public String mergeMedia(List<MultipartFile> files, String fileExtension) {
        try {
            if (files.isEmpty()) {
                return "No files provided for merging.";
            }
            if (!fileExtension.equals(".mp3") && !fileExtension.equals(".mp4")) {
                return "Unsupported file format. Only .mp3 and .mp4 are allowed.";
            }
            String resourceType = fileExtension.equals(".mp3") ? "audio" : "video";
            File listFile = File.createTempFile("fileList", ".txt");
            StringBuilder listContent = new StringBuilder();
            List<File> tempFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                File tempFile = File.createTempFile("temp_media_", fileExtension);
                file.transferTo(tempFile);
                tempFiles.add(tempFile);
                listContent.append("file '").append(tempFile.getAbsolutePath()).append("'\n");
            }

            Files.write(listFile.toPath(), listContent.toString().getBytes());
            File outputFile = File.createTempFile("merged_", fileExtension);
            String ffmpegCommand = String.format(
                    "ffmpeg -y -f concat -safe 0 -i %s -c copy %s",
                    listFile.getAbsolutePath(), outputFile.getAbsolutePath()
            );

            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegCommand.split(" "));
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();
            MultipartFile multipartOutputFile = new MockMultipartFile(
                    outputFile.getName(),
                    outputFile.getName(),
                    "application/octet-stream",
                    Files.readAllBytes(outputFile.toPath())
            );

            String cloudinaryUrl = cloudinaryService.uploadFile(multipartOutputFile,resourceType);
            listFile.delete();
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }
            outputFile.delete();

            return cloudinaryUrl != null ? cloudinaryUrl : "Error uploading merged file to Cloudinary.";
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error while merging media.";
        }
    }
    @Override
    public String mixAudioVideo(String videoFile, String audioFile, String outputFile) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", "\"" + videoFile + "\"",
                    "-i", "\"" + audioFile + "\"",
                    "-c:v", "copy",
                    "-c:a", "aac",
                    "-map", "0:v:0",
                    "-map", "1:a:0",
                    outputFile
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            StringBuilder processOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    processOutput.append(line).append("\n");
                }
            }
            return "Merge successful, output file: " + outputFile;
        } catch (Exception e) {
            e.printStackTrace();
            return "Merge failed: " + e.getMessage();
        }
    }
    @Override
    public MultipartFile addSrtToVideo(MultipartFile videoFile, MultipartFile srtFile) {
        try {
            File tempVideoFile = File.createTempFile("temp_video_", ".mp4");
            File tempSubtitleFile = File.createTempFile("temp_subtitle_", ".srt");
            videoFile.transferTo(tempVideoFile);
            srtFile.transferTo(tempSubtitleFile);
            File outputFile = File.createTempFile("output_burned_", ".mp4");
            String subtitlePath = tempSubtitleFile.getAbsolutePath();

            // Escape the subtitle file path:
            // - Replace "\" with "\\" to handle backslashes in Windows paths.
            // - Replace ":" with "\:" to prevent issues in certain systems.
            String escapedSubtitlePath = subtitlePath.replace("\\", "\\\\").replace(":", "\\:");

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-i", tempVideoFile.getAbsolutePath(),
                    "-vf", "subtitles='" + escapedSubtitlePath + "'",
                    "-c:v", "libx264", "-crf", "23", "-preset", "fast",
                    "-c:a", "copy", outputFile.getAbsolutePath()
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                output.append(line).append("\n");
            }
            MultipartFile mergedFile = convertFileToMultipartFile(outputFile);
            tempVideoFile.delete();
            tempSubtitleFile.delete();
            outputFile.delete();
            return mergedFile;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String createFullVideo(Project project, String outputVideoPath) {
        String black_video = createBlackBackgroundVideo(project.getLength());
        String merge_video = processVideos(project, black_video);
        String newOutput = "output" + ".mp4";
        if (project.getBackground() != null) {
            addBackground(project.getBackground().getAsset(),
                    merge_video,
                    newOutput);
        }
        newOutput = convertVideo(newOutput, outputVideoPath);
        File outputFile = new File(newOutput);
        if (outputFile.exists()) {
            project.setAsset(cloudinaryService.uploadFile(outputFile, MediaType.VIDEO.getname()));
            projectRepo.save(project);
            deleteFileIfExists(newOutput);
        } else {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
        return project.getAsset();
    }

    private String convertVideo(String inputVideoPath, String outputFileExtension) {
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
        deleteFileIfExists(inputVideoPath);
        return outputVideoPath;
    }

    private String processVideos(Project project, String videoPath) {
        int index = 1;
        for (Video video : project.getVideo()) {
            String newOutput = "output_" + index++ + ".mp4";
            videoPath = overlayVideo(videoPath, video.getAsset(), video.getStartTime(), newOutput);
        }
        return videoPath;
    }

    private String overlayVideo(String backgroundVideoPath, String overlayVideoPath, double startTime, String outputPath) {
        try {
            runFFmpegCommand(Arrays.asList(
                    "ffmpeg", "-y",
                    "-i", backgroundVideoPath,
                    "-i", overlayVideoPath,
                    "-filter_complex",
                    "[1:v]setpts=PTS-STARTPTS+" + startTime + "/TB[v1]; " +
                            "[1:a]adelay=" + (int)(startTime * 1000) + "|" + (int)(startTime * 1000) + "[a1]; " +
                            "[0:a][a1]amix=inputs=2:duration=first[aout]; " +
                            "[0:v][v1]overlay=W/2-w/2:H/2-h/2[v]",
                    "-map", "[v]",
                    "-map", "[aout]",
                    "-c:v", "libx264",
                    "-c:a", "aac", "-b:a", "192k",
                    outputPath
            ));
            deleteFileIfExists(backgroundVideoPath);
            return outputPath;
        } catch (IOException | InterruptedException e) {
            throw new AppException(ErrorCode.FFMPEG_OVERLAY_VIDEO_FAIL);
        }
    }

    private String createBlackBackgroundVideo(Double duration) {
        try {
            String outputPath = "black_video" + ".mp4";
            runFFmpegCommand(Arrays.asList(
                    "ffmpeg", "-y",
                    "-f", "lavfi", "-t", String.valueOf(duration), "-i", "color=c=black:s=1280x720",
                    "-f", "lavfi", "-t", String.valueOf(duration), "-i", "anullsrc=r=44100:cl=stereo",
                    "-c:v", "libx264", "-c:a", "aac",
                    outputPath
            ));
            return outputPath;
        } catch (IOException | InterruptedException e) {
            throw new AppException(ErrorCode.FFMPEG_CREATE_VIDEO_FAIL);
        }
    }

    private String addBackground (String backgroundPath, String videoPath, String outputPath) {
        try{
            runFFmpegCommand(Arrays.asList(
                    "ffmpeg", "-y",
                    "-i", backgroundPath,
                    "-i", videoPath,
                    "-filter_complex", "[0:v]scale=1280:720[bg];[1:v]scale=1280:720[fg];[fg][bg]overlay=(W-w)/2:(H-h)/2[out]",
                    "-map", "[out]", "-map", "1:a",
                    "-c:v", "libx264", "-crf", "18", "-preset", "ultrafast",
                    "-c:a", "aac",
                    outputPath
            ));
        }catch (Exception e){
            e.printStackTrace();
        }
        deleteFileIfExists(videoPath);
        return outputPath;
    }

    private void runFFmpegCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        outputThread.start();
        errorThread.start();
        int exitCode = process.waitFor();
        outputThread.join();
        errorThread.join();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg execution failed:\n" + error.toString());
        }
    }
    private void deleteFileIfExists(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File deleted successfully.");
            } else {
                System.out.println("Failed to delete the file.");
            }
        } else {
            System.out.println("File does not exist.");
        }
    }
}