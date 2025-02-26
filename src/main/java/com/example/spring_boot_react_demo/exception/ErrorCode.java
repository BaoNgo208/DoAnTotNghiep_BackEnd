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
    BACKGROUND_NOT_FOUND("background not found", HttpStatus.NOT_FOUND)
    ;

    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(String message, HttpStatusCode httpStatusCode) {
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}