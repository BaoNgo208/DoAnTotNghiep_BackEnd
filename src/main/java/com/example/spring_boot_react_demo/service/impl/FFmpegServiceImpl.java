package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.model.entity.Video;
import com.example.spring_boot_react_demo.repository.VideoRepo;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.FFmpegService;
import com.example.spring_boot_react_demo.util.ConvertUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FFmpegServiceImpl implements FFmpegService {
    private final CloudinaryService cloudinaryService;
    private final VideoRepo videoRepo;

    @Override
    public String extractAudio(String videoPath) {
        String audioPath = videoPath.replace(".mp4", ".mp3");
        String command = "ffmpeg -i " + videoPath + " -vn -acodec mp3 " + audioPath;

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            return "Audio extraction completed, output saved to: " + audioPath;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error during audio extraction";
        }
    }

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

            String cloudinaryUrl = cloudinaryService.uploadFile(multipartOutputFile, "folder_1",resourceType);
            listFile.delete();
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }
            outputFile.delete();

            Video newVideo = new Video();
            newVideo.setUrl(cloudinaryUrl);
            videoRepo.save(newVideo);

            return cloudinaryUrl != null ? cloudinaryUrl : "Error uploading merged file to Cloudinary.";
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error while merging media.";
        }
    }

    @Override
    public String convertAndUploadVideo(String inputVideoPath, String outputFileName) {
        try {
            String outputVideoPath =  createDirectory() + File.separator + outputFileName;
            executeFFmpegCommand(inputVideoPath, outputVideoPath);
            File outputFile = new File(outputVideoPath);
            if (!outputFile.exists() || outputFile.length() == 0) {
                return "Video conversion failed. The output file is empty.";
            }
            MultipartFile multipartOutputFile = new MockMultipartFile(
                    outputFile.getName(), outputFile.getName(), "video/mp4",
                    Files.readAllBytes(outputFile.toPath())
            );
            String cloudinaryUrl = cloudinaryService.export(multipartOutputFile,
                    "converted_videos",
                    "video");
            deleteFileIfExists(outputFile);

            if (cloudinaryUrl != null && !cloudinaryUrl.isEmpty()) {
                Video newVideo = new Video();
                newVideo.setUrl(cloudinaryUrl);
                videoRepo.save(newVideo);
                return cloudinaryUrl;
            } else {
                return "Error uploading to Cloudinary.";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error while converting video: " + e.getMessage();
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
    public String addSrtToVideo(MultipartFile videoFile, MultipartFile srtFile) {
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
            MultipartFile mergedFile = ConvertUtils.convertFileToMultipartFile(outputFile);
            String cloudinaryUrl = cloudinaryService.uploadFile(mergedFile, "folder_1","video");
            tempVideoFile.delete();
            tempSubtitleFile.delete();
            outputFile.delete();
            return cloudinaryUrl != null ? cloudinaryUrl : "Error uploading merged file to Cloudinary.";

        } catch (IOException e) {
            e.printStackTrace();
            return " Error during processing: " + e.getMessage();
        }
    }

    private String createDirectory() {
        String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads";
        try {
            Files.createDirectories(Paths.get(uploadsDir));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return uploadsDir;
    }

    private void executeFFmpegCommand(String inputPath, String outputPath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", inputPath, outputPath);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        readFFmpegLogs(process);
    }

    private void readFFmpegLogs(Process process)  {
        StringBuilder logOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logOutput.append(line).append("\n");
            }
            int exitCode = 0;
            exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("FFmpeg error: \n" + logOutput);
                throw new RuntimeException("\n" + "Video conversion failed");
            }
        } catch (IOException| InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(logOutput);
    }

    private void deleteFileIfExists(File file) {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            System.err.println("Cannot delete file: " + file.getAbsolutePath());
        }
    }
}