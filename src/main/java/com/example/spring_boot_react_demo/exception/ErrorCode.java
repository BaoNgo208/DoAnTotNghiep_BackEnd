package com.example.spring_boot_react_demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    INVALID_IMAGE_FORMAT("Invalid image format", HttpStatus.BAD_REQUEST),
    INVALID_VIDEO_FORMAT("Invalid video format", HttpStatus.BAD_REQUEST),
    INVALID_AUDIO_FORMAT("Invalid audio format", HttpStatus.BAD_REQUEST),
    INVALID_BACKGROUND_FORMAT("Invalid background format", HttpStatus.BAD_REQUEST),
    PROJECT_NOT_FOUND("Project not found", HttpStatus.NOT_FOUND),
    BACKGROUND_NOT_FOUND("background not found", HttpStatus.NOT_FOUND),
    VIDEO_NOT_FOUND("video not found", HttpStatus.NOT_FOUND),
    FFMPEG_CREATE_VIDEO_FAIL("FFmpeg processing failed  for create video", HttpStatus.BAD_REQUEST),
    FFMPEG_OVERLAY_VIDEO_FAIL("FFmpeg processing failed  for overlay video", HttpStatus.BAD_REQUEST),
    PROCESS_FAILED("Process failed", HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_FILE_FAIL("Cannot delete file", HttpStatus.INTERNAL_SERVER_ERROR),
    CREATE_FILE_FAIL("Cannot create file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND("File not found", HttpStatus.NOT_FOUND),
    PROJECT_HAS_NO_LYRICS("Project has no lyrics", HttpStatus.NOT_FOUND)
    ;

    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(String message, HttpStatusCode httpStatusCode) {
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}