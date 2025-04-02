package com.example.spring_boot_react_demo.enums;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;

public enum FFmpegTransition {
    FADE("fade"),
    DISSOLVE("dissolve");

    private final String transitionName;

    FFmpegTransition(String transitionName) {
        this.transitionName = transitionName;
    }

    public String getTransitionName() {
        return transitionName;
    }

    @Override
    public String toString() {
        return transitionName;
    }

    public static FFmpegTransition fromString(String type) {
        for (FFmpegTransition t : values()) {
            if (t.name().equalsIgnoreCase(type)) {
                return t;
            }
        }
        throw new AppException(ErrorCode.INVALID_TRANSITION_TYPE);
    }
}
