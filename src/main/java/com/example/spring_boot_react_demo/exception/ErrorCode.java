package com.example.spring_boot_react_demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    INVALID_VIDEO_FORMAT("Invalid video format", HttpStatus.BAD_REQUEST),
    PROJECT_NOT_FOUND("Project not found", HttpStatus.NOT_FOUND),
    VIDEO_NOT_FOUND("video not found", HttpStatus.NOT_FOUND),
    FFMPEG_CREATE_VIDEO_FAIL("FFmpeg processing failed  for create video", HttpStatus.BAD_REQUEST),
    PROJECT_HAS_NO_LYRICS("Project has no lyrics", HttpStatus.NOT_FOUND)
    ;

    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(String message, HttpStatusCode httpStatusCode) {
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}