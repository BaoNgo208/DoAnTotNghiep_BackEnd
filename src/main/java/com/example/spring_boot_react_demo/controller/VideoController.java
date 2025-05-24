package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.model.dto.request.AddVideosRequest;
import com.example.spring_boot_react_demo.model.dto.request.ApplyTransitionRequest;
import com.example.spring_boot_react_demo.model.dto.response.ApiResponse;
import com.example.spring_boot_react_demo.model.dto.response.VideoResponse;
import com.example.spring_boot_react_demo.model.entity.Project;
import com.example.spring_boot_react_demo.model.entity.Video;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.FFmpegService;
import com.example.spring_boot_react_demo.service.ProjectService;
import com.example.spring_boot_react_demo.service.VideoService;
import com.example.spring_boot_react_demo.util.FileUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoController {
    VideoService videoService;
    FFmpegService fFmpegService;
    CloudinaryService cloudinaryService;
    ProjectService projectService;

    @PostMapping()
    public ApiResponse<List<VideoResponse>> addVideo(@RequestBody AddVideosRequest request) {
        return ApiResponse.<List<VideoResponse>>builder()
                .result(videoService.addVideo(request))
                .build();
    }

    @DeleteMapping()
    public ApiResponse<String> deleteVideo(@RequestParam Long videoId) {
        return ApiResponse.<String>builder()
                .result(videoService.deleteVideo(videoId))
                .build();
    }

    @PostMapping("/applyTransition")
    public ApiResponse<?> applyTransition(@RequestBody ApplyTransitionRequest applyTransitionRequest) throws IOException {

        MultipartFile result = videoService.applyTransition(applyTransitionRequest);
        return ApiResponse.builder()
                .result(Base64.getEncoder().encodeToString(result.getBytes()))
                .build();
    }

    @PostMapping("/removeEffect")
    public ApiResponse<?> removeEffect(@RequestParam Long projectId) throws IOException {
        MultipartFile result = videoService.removeEffect(projectId);
        return ApiResponse.builder()
                .result(Base64.getEncoder().encodeToString(result.getBytes()))
                .build();
    }

    @PostMapping("/addBackground")
    public ApiResponse<?> addBackground(@RequestParam String videoPath, @RequestParam String backgroundPath, @RequestParam Long videoId) throws IOException {
        return ApiResponse.builder()
                .result(videoService.addBackground(videoPath, backgroundPath, videoId))
                .build();
    }

    @PostMapping("/removeBackground")
    public ApiResponse<?> removeBackground(@RequestParam String videoPath, @RequestParam Long videoId) {
        return ApiResponse.builder()
                .result(videoService.removeBackground(videoPath, videoId))
                .build();
    }

    @PostMapping("/addAudioToVideo")
    public ResponseEntity<String> mixAudioWithVideo(
            @RequestParam("videoId") Long videoId,
            @RequestParam("videoUrl") String videoUrl,
            @RequestParam("audioFile") MultipartFile audioFile) {

        Path videoPath = Paths.get("input_video.mp4");
        Path audioPath = Paths.get("new_audio.mp3");
        Path outputPath = Paths.get("output_video.mp4");

        try {
            // 1. Tải video từ Cloudinary
            try (InputStream in = new URL(videoUrl).openStream()) {
                Files.copy(in, videoPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 2. Lưu audio vào file
            Files.copy(audioFile.getInputStream(), audioPath, StandardCopyOption.REPLACE_EXISTING);

            // 3. Gọi FFmpeg để mix audio
            fFmpegService.mixAudioWithVideo(videoPath,audioPath,outputPath);
            String uploadedUrl = cloudinaryService.uploadFile(outputPath.toFile(), "video");


            Video previousVideo = videoService.getVideoById(videoId);
            String previousAssetUrl = previousVideo.getAsset();

            Pattern oldPattern = Pattern.compile("/so_(\\d+(\\.\\d+)?),eo_(\\d+(\\.\\d+)?)/v(\\d+)/");
            Matcher oldMatcher = oldPattern.matcher(previousAssetUrl);

            if (!oldMatcher.find()) {
                throw new IllegalStateException("Không thể tìm thấy so, eo, version trong previous asset URL: " + previousAssetUrl);
            }

            String so = oldMatcher.group(1); // "0"
            String eo = oldMatcher.group(3); // "25.042"
            String version = oldMatcher.group(5); // "1747234899"

// --- Lấy publicId từ uploaded URL ---
            Pattern newPattern = Pattern.compile("/v\\d+/(.+?)\\.mp4$");
            Matcher newMatcher = newPattern.matcher(uploadedUrl);

            if (!newMatcher.find()) {
                throw new IllegalStateException("Không thể trích xuất publicId từ uploadedUrl: " + uploadedUrl);
            }

            String newPublicId = newMatcher.group(1); // sẽ ra "vmhadl081ebv9se7l0ws"


// --- Ghép URL mới ---
            String finalUrl = "https://res.cloudinary.com/dn3umiee1/video/upload/"
                    + "so_" + so + ",eo_" + eo + "/v" + version + "/" + newPublicId ;

// Cập nhật asset mới
            previousVideo.setAsset(finalUrl);
            videoService.saveVideo(previousVideo);
            return ResponseEntity.ok(finalUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());

        } finally {
            FileUtil.deleteFileIfExists(videoPath.toString());
            FileUtil.deleteFileIfExists(audioPath.toString());
            FileUtil.deleteFileIfExists(outputPath.toString());

        }
    }


    @PostMapping("/addAudiosToVideo")
    public ResponseEntity<String> mixAudiosWithVideo(
            @RequestParam("videoId") Long videoId,
            @RequestParam("audioFiles") List<MultipartFile> audioFiles,
            @RequestParam("audioStartTime") List<Double> audioStartTime
    ) {

        Path videoPath = Paths.get("input_video.mp4");
        Path outputPath = Paths.get("output_video.mp4");
        List<Path> audioPaths = new ArrayList<>();
        Project previousProject = projectService.getProjectById(videoId);


        try {
            // 1. Tải video từ Cloudinary
            try (InputStream in = new URL(previousProject.getAsset()).openStream()) {
                Files.copy(in, videoPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 2. Lưu từng audio file
            for (int i = 0; i < audioFiles.size(); i++) {
                Path audioPath = Paths.get("audio_" + i + ".mp3");
                Files.copy(audioFiles.get(i).getInputStream(), audioPath, StandardCopyOption.REPLACE_EXISTING);
                audioPaths.add(audioPath);
            }
            List<Integer> startTimesInMs = List.of(0, 29000); // audio1 từ 0s, audio2 từ 5s
            List<Double> audioStartTimesInMs = audioStartTime
                    .stream()
                    .map(start -> (double) (start * 1000))
                    .toList();

            // 3. Gọi FFmpeg để mix audio
            fFmpegService.mixAudiosWithTiming(videoPath, audioPaths,audioStartTimesInMs, outputPath);
            String uploadedUrl = cloudinaryService.uploadFile(outputPath.toFile(), "video");

            // 4. Xử lý URL


            previousProject.setAsset(uploadedUrl);
            projectService.saveProject(previousProject);
            return ResponseEntity.ok(uploadedUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        } finally {
            FileUtil.deleteFileIfExists(videoPath.toString());
            FileUtil.deleteFileIfExists(outputPath.toString());
            for (Path audioPath : audioPaths) {
                FileUtil.deleteFileIfExists(audioPath.toString());
            }
        }
    }

    @PostMapping("/addVintageEffect")
    public ResponseEntity<String> applyVintageEffect(@RequestParam("videoId") Long videoId) throws IOException, InterruptedException {
        return ResponseEntity.ok(videoService.applyVintageEffect(videoId)) ;
    }

    @PostMapping("/addVintageEffectWithOverlay")
    public ResponseEntity<String> applyVintageEffectWithOverlay(@RequestParam("videoId") Long videoId , @RequestParam("overlayUrl") String overlayUrl) throws  IOException,InterruptedException
    {
        return ResponseEntity.ok(videoService.applyVintageEffectWithOverlay(videoId,overlayUrl));
    }
}