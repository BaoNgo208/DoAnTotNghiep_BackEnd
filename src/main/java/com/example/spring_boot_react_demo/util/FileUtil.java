package com.example.spring_boot_react_demo.util;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileUtil {

    public static void deleteFileIfExists(String filePath) {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Path path = Paths.get(filePath);

                if (Files.exists(path)) {
                    if (Files.deleteIfExists(path)) {
                        log.info("File deleted successfully: " + filePath);
                    }
                } else {
                    log.info("File does not exist: " + filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}