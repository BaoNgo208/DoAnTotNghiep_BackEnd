package com.example.spring_boot_react_demo.exception;

public class AppException extends RuntimeException {

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    private ErrorCode errorCode;
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}