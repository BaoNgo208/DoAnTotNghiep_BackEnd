package com.example.spring_boot_react_demo.util;
import com.example.spring_boot_react_demo.model.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class FileUtil {

    private static final List<String> IMAGE_TYPES = Arrays.asList(
            "image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp"
    );

    private static final List<String> VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/x-matroska", "video/webm", "video/avi"
    );

    private static final List<String> AUDIO_TYPES = Arrays.asList(
            "audio/mpeg", "audio/ogg", "audio/wav", "audio/mp3", "audio/x-ms-wma"
    );

    public static boolean isImage(MultipartFile file) {
        return file != null && !file.isEmpty() && IMAGE_TYPES.contains(file.getContentType());
    }

    public static boolean isVideo(MultipartFile file) {
        return file != null && !file.isEmpty() && VIDEO_TYPES.contains(file.getContentType());
    }

    public static boolean isAudio(MultipartFile file) {
        return file != null && !file.isEmpty() && AUDIO_TYPES.contains(file.getContentType());
    }

    public static String getFileType(MultipartFile file) {
        if (isImage(file)) {
            return MediaType.IMAGE.getname();
        } else if (isVideo(file)) {
            return MediaType.VIDEO.getname();
        } else if (isAudio(file)) {
            return MediaType.AUDIO.getname();
        } else {
            return "unknown";
        }
    }

    public static File createSrcFile(String text) {
        File file = new File("lyrics.srt");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
    public static MultipartFile createSrtMultipartFile(String text) {
        File file = createSrcFile(text);
        try {
            byte[] content = Files.readAllBytes(file.toPath());
            return new MockMultipartFile("lyrics.srt", "lyrics.srt", "text/plain", content);
        } catch (IOException e) {
            throw new RuntimeException("Error converting file to MultipartFile: " + e.getMessage());
        }
    }
}