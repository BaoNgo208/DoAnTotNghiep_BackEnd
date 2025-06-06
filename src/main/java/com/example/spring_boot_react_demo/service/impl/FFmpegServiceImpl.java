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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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

    @Override
    public void mixAudioWithVideo(Path videoPath, Path audioPath, Path outputPath) throws IOException, InterruptedException {
        List<String> command = Arrays.asList(
                "ffmpeg",
                "-i", videoPath.toString(),
                "-i", audioPath.toString(),
                "-filter_complex", "[0:a][1:a]amix=inputs=2:duration=first:dropout_transition=2[a]",
                "-map", "0:v",
                "-map", "[a]",
                "-c:v", "copy",
                "-shortest",
                outputPath.toString()
        );

        runFFmpegCommand(command);
    }

    public void mixAudiosWithTiming(Path videoPath, List<Path> audioPaths, List<Double> startTimesInMs, Path outputPath) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");

        // Input video
        command.add("-i");
        command.add(videoPath.toString());

        // Input audios
        for (Path audioPath : audioPaths) {
            command.add("-i");
            command.add(audioPath.toString());
        }

        // Build filter_complex
        StringBuilder filter = new StringBuilder();

        // 1. Gắn audio gốc từ video
        filter.append("[0:a]volume=0.5,adelay=0|0[a0];");

        // 2. Gắn các audio thêm, với thời gian delay tương ứng
        for (int i = 0; i < audioPaths.size(); i++) {
            Double delay = startTimesInMs.get(i);
            filter.append("[").append(i + 1).append(":a]")
                    .append("volume=1.5,adelay=") // tăng 150%
                    .append(delay).append("|").append(delay)
                    .append("[a").append(i + 1).append("];");
        }

        // 3. Gộp tất cả audio lại
        for (int i = 0; i <= audioPaths.size(); i++) {
            filter.append("[a").append(i).append("]");
        }

        filter.append("amix=inputs=").append(audioPaths.size() + 1)
                .append(":duration=first:dropout_transition=2[aout]");

        // Thêm filter_complex
        command.add("-filter_complex");
        command.add(filter.toString());

        // Map video stream và audio sau khi mix
        command.add("-map");
        command.add("0:v");
        command.add("-map");
        command.add("[aout]");

        command.add("-c:v");
        command.add("copy");
        command.add("-shortest");

        command.add(outputPath.toString());

        runFFmpegCommand(command);
    }



    @Override
    public MultipartFile applyVintageEffect(String videoUrl) throws IOException, InterruptedException {
        Path workingDir = Paths.get(System.getProperty("user.dir"));

        Path inputPath = workingDir.resolve("input_video.mp4");
        Path outputPath = workingDir.resolve("output_video.mp4");

        try (InputStream in = new URL(videoUrl).openStream()) {
            Files.copy(in, inputPath, StandardCopyOption.REPLACE_EXISTING);
        }

        List<String> command = Arrays.asList(
                "ffmpeg",
                "-i", inputPath.toString(),
                "-vf", "curves=vintage,vignette,eq=contrast=1.2:brightness=0.05:saturation=0.6",
                "-c:a", "aac",
                "-c:v", "libx264",
                outputPath.toString()
        );
        runFFmpegCommand(command);


        MultipartFile resultFile;
        try (InputStream fis = Files.newInputStream(outputPath)) {
            resultFile = new MockMultipartFile("file", outputPath.getFileName().toString(), "video/mp4", fis);
        }

        // Xóa file tạm sau khi đã tạo MultipartFile xong
        try {
            Files.deleteIfExists(inputPath);
            Files.deleteIfExists(outputPath);
        } catch (IOException e) {
            e.printStackTrace();  // hoặc log lỗi tùy bạn
        }

        return resultFile;
    }
    private MultipartFile processVideoWithOverlay(String videoUrl, String overlayUrl, FFmpegCommandBuilder commandBuilder)
            throws IOException, InterruptedException {

        Path workingDir = Paths.get(System.getProperty("user.dir"));
        Path inputPath = workingDir.resolve("input_video.mp4");
        Path overlayPath = workingDir.resolve("overlay_video.mp4");
        Path outputPath = workingDir.resolve("output_video.mp4");

        try (InputStream in = new URL(videoUrl).openStream()) {
            Files.copy(in, inputPath, StandardCopyOption.REPLACE_EXISTING);
        }

        try (InputStream in = new URL(overlayUrl).openStream()) {
            Files.copy(in, overlayPath, StandardCopyOption.REPLACE_EXISTING);
        }

        List<String> command = commandBuilder.build(inputPath, overlayPath, outputPath);
        runFFmpegCommand(command);

        MultipartFile resultFile;
        try (InputStream fis = Files.newInputStream(outputPath)) {
            resultFile = new MockMultipartFile("file", outputPath.getFileName().toString(), "video/mp4", fis);
        }

        try {
            Files.deleteIfExists(inputPath);
            Files.deleteIfExists(overlayPath);
            Files.deleteIfExists(outputPath);
        } catch (IOException e) {
            e.printStackTrace();  // hoặc log lỗi
        }

        return resultFile;
    }

    @FunctionalInterface
    public interface FFmpegCommandBuilder {
        List<String> build(Path inputPath, Path overlayPath, Path outputPath);
    }
    @Override
    public MultipartFile applyVintageEffectWithOverlay(String videoUrl, String overlayUrl) throws IOException, InterruptedException {
        return processVideoWithOverlay(videoUrl, overlayUrl, (input, overlay, output) -> Arrays.asList(
                "ffmpeg",
                "-stream_loop", "-1",
                "-i", overlay.toString(),
                "-i", input.toString(),
                "-filter_complex", "[1:v]format=yuv420p,scale=1920:1080,setsar=1[base];" +
                        "[0:v]format=gray,scale=1920:1080:flags=bicubic,setsar=1[scratch];" +
                        "[base][scratch]blend=all_mode='overlay':all_opacity=0.2[v];" +
                        "[v]colorchannelmixer=.9:.5:.3:0:.4:.8:.2:0:.2:.3:.7[out]",
                "-map", "[out]",
                "-map", "1:a?",
                "-c:v", "libx264",
                "-c:a", "copy",
                "-shortest",
                output.toString()
        ));
    }

    @Override
    public MultipartFile applyRetroCameraEffect(String videoUrl, String overlayUrl) throws IOException, InterruptedException {
        return processVideoWithOverlay(videoUrl, overlayUrl, (input, overlay, output) -> Arrays.asList(
                "ffmpeg",
                "-i", input.toString(),
                "-stream_loop", "-1",
                "-i", overlay.toString(),
                "-filter_complex",
                "[1]chromakey=0x008000:blend=0:similarity=0.15[ckout];" +
                        "[ckout][0]scale2ref[ckout_scaled][base_scaled];" +
                        "[base_scaled][ckout_scaled]overlay",
                "-shortest",
                output.toString()
        ));
    }


    @Override
    public MultipartFile applyRetroCameraEffectWithVintage(String videoUrl, String overlayUrl) throws IOException, InterruptedException {
        return processVideoWithOverlay(videoUrl, overlayUrl, (input, overlay, output) -> Arrays.asList(
                "ffmpeg",
                "-i", input.toString(),
                "-stream_loop", "-1",
                "-i", overlay.toString(),
                "-filter_complex",
                "[1]chromakey=0x008000:blend=0:similarity=0.15[ckout];" +
                        "[ckout][0]scale2ref[ckout_scaled][base_scaled];" +
                        "[base_scaled][ckout_scaled]overlay," +
                        "format=yuv420p,eq=saturation=0.6:contrast=1.1:brightness=0.05," +
                        "curves=vintage,noise=alls=20:allf=t+u",
                "-shortest",
                output.toString()
        ));
    }

    @Override
    public MultipartFile applyNaturalFallEffect(String videoUrl, String overlayUrl, String fallType) throws IOException, InterruptedException {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        Path inputPath = workingDir.resolve("input_video.mp4");
        Path overlayPath = workingDir.resolve("overlay_video.mp4");
        Path outputPath = workingDir.resolve("output_video.mp4");

        // Tải video chính về
        try (InputStream in = new URL(videoUrl).openStream()) {
            Files.copy(in, inputPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Tải overlay (hiệu ứng) về
        try (InputStream in = new URL(overlayUrl).openStream()) {
            Files.copy(in, overlayPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Xây dựng lệnh FFmpeg
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-stream_loop");
        command.add("-1");
        command.add("-i");
        command.add(overlayPath.toString());
        command.add("-i");
        command.add(inputPath.toString());

        if ("snow".equalsIgnoreCase(fallType)) {
            command.add("-filter_complex");
            command.add("[0:v]colorkey=0x000000:0.3:0.1[snow];[1:v][snow]overlay=0:0:shortest=1");
        } else {
            command.add("-filter_complex");
            command.add("[0:v]colorkey=0x000000:0.1:0.05,format=rgba,colorchannelmixer=aa=0.7[sakura];[1:v][sakura]overlay=0:0:shortest=1");
        }

        command.add(outputPath.toString());

        // Chạy FFmpeg
        runFFmpegCommand(command);

        // Trả về kết quả dạng MultipartFile
        MultipartFile resultFile;
        try (InputStream fis = Files.newInputStream(outputPath)) {
            resultFile = new MockMultipartFile("file", outputPath.getFileName().toString(), "video/mp4", fis);
        }

        // Xoá file tạm nếu cần
        Files.deleteIfExists(inputPath);
        Files.deleteIfExists(overlayPath);
        Files.deleteIfExists(outputPath);

        return resultFile;
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
        System.out.println("Running FFmpeg command: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[ffmpeg] " + line);  // in từng dòng output
                output.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor();
        System.out.println("FFmpeg process exited with code: " + exitCode);

        if (exitCode != 0) {
            System.err.println("FFmpeg failed. Full output:\n" + output.toString());
            throw new RuntimeException("FFmpeg execution failed with exit code: " + exitCode);
        }

        return output.toString();
    }


}