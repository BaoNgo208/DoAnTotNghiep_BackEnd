package com.example.spring_boot_react_demo.util;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
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
}
