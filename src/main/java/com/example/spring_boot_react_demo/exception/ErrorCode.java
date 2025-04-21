package com.example.spring_boot_react_demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    INVALID_VIDEO_FORMAT("Invalid video format", HttpStatus.BAD_REQUEST),
    PROJECT_NOT_FOUND("Project not found", HttpStatus.NOT_FOUND),
    VIDEO_NOT_FOUND("video not found", HttpStatus.NOT_FOUND),
    FFMPEG_ADD_LYRIC_FAIL("FFmpeg processing failed  for add Ass to video", HttpStatus.BAD_REQUEST),
    FFMPEG_GET_DURATION_VIDEO_FAIL("FFmpeg processing failed  get VideoDuration", HttpStatus.BAD_REQUEST),
    PROJECT_HAS_NO_LYRICS("Project has no lyrics", HttpStatus.NOT_FOUND),
    INVALID_TRANSITION_TYPE("Invalid transition type", HttpStatus.BAD_REQUEST),
    NO_VIDEOS_PROVIDED("No videos provided", HttpStatus.BAD_REQUEST),
    CANNOT_APPLY_TRANSITION("Cannot apply transition effect to the project", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("Unauthorized: Token expired or invalid", HttpStatus.UNAUTHORIZED),
    EFFECT_DOES_NOT_EXIT("Effect does not exit", HttpStatus.BAD_REQUEST),
    BACKGROUND_DOSE_NOT_EXIST("Background dose does not exist", HttpStatus.BAD_REQUEST),
    ;

    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(String message, HttpStatusCode httpStatusCode) {
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}