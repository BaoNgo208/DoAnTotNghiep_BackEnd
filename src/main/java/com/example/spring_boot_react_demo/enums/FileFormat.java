package com.example.spring_boot_react_demo.enums;

import java.util.EnumSet;

public enum FileFormat {
    PNG("image/png"), JPEG("image/jpeg"), GIF("image/gif"), WEBP("image/webp"),
    MP4("video/mp4"), MPEG("video/mpeg"), AVI("video/avi"),
    MP3("audio/mpeg"), WAV("audio/wav"), OGG("audio/ogg");

    private final String mimeType;

    FileFormat(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static boolean isValidFormat(String mimeType) {
        return EnumSet.allOf(FileFormat.class).stream()
                .anyMatch(format -> format.getMimeType().equals(mimeType));
    }
}
