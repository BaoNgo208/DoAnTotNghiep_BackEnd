package com.example.spring_boot_react_demo.util;

import okhttp3.MediaType;
import org.springframework.beans.factory.annotation.Value;

public final class Constants {
    private Constants() {
    }

    public static final String DOT= ".";
    public static final String CLOUDINARY_UPLOAD_URL = "https://res.cloudinary.com/dnuyd3qxz/";
    public static final String LOCAL_UPLOAD_URL = "/upload/v1/";
    public static final int NOT_FOUND = -1;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final String API_URL = "https://api.openai.com/v1/audio/transcriptions";
    public static final MediaType MEDIA_TYPE_AUDIO = MediaType.parse("audio/mpeg");
    public static final String OPENAI_API_KEY = "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";
    public static final String HEADER_FILE_PATH = "src/main/java/com/example/spring_boot_react_demo/util/ass_header.txt";
}