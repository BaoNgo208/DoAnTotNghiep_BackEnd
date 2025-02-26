package com.example.spring_boot_react_demo.util;

import okhttp3.MediaType;

public final class Constants {
    private Constants() {
    }

    public static final String DOT= ".";
    public static final String CLOUDINARY_UPLOAD_URL = "https://res.cloudinary.com/duli95mss/";
    public static final String LOCAL_UPLOAD_URL = "/upload/v1/";
    public static final int NOT_FOUND = -1;
    public static final int ZERO = 0;
    public static final String OK = "ok";
    public static final String RESULT = "result";
    public static final String API_URL = "https://api.openai.com/v1/audio/transcriptions";
    public static final MediaType MEDIA_TYPE_AUDIO = MediaType.parse("audio/mpeg");
    public static final String OPENAI_API_KEY = "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";
}
