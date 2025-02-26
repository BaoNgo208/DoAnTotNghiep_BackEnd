package com.example.spring_boot_react_demo.util;
import com.example.spring_boot_react_demo.model.File;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;

public class FileUtil {

    private static final List<String> IMAGE_TYPES = Arrays.asList(
            "image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp"
    );

    private static final List<String> VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/x-matroska", "video/webm"
    );

    private static final List<String> AUDIO_TYPES = Arrays.asList(
            "audio/mpeg", "audio/ogg", "audio/wav", "audio/mp4", "audio/x-ms-wma"
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
            return File.IMAGE.name();
        } else if (isVideo(file)) {
            return File.VIDEO.name();
        } else if (isAudio(file)) {
            return File.AUDIO.name();
        } else {
            return "unknown";
        }
    }
}
