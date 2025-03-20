package com.example.spring_boot_react_demo.util;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConvertUtils {
    public static MultipartFile convertFileToMultipartFile(File file) throws IOException {
        return new MockMultipartFile(
                file.getName(),
                file.getName(),
                "video/mp4",
                new FileInputStream(file)
        );
    }
    public static File convertMultipartFileToFile(MultipartFile multipartFile, String path) {
        File file = new File(path);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert MultipartFile to File", e);
        }
        return file;
    }
}
