package com.example.spring_boot_react_demo.enums;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;

public enum FFmpegTransition {
    FADE("fade"),
    DISSOLVE("dissolve"),
    WIPELEFT("wipeleft"),
    WIPERIGHT("wiperight"),
    WIPEUP("wipeup"),
    WIPEDOWN("wipedown"),
    SLIDELEFT("slideleft"),
    SLIDERIGHT("slideright"),
    SLIDEUP("slideup"),
    SLIDEDOWN("slidedown"),
    CIRCLECROP("circlecrop"),
    RECTCROP("rectcrop"),
    DISTANCE("distance"),
    FADEBLACK("fadeblack"),
    FADEWHITE("fadewhite"),
    RADIAL("radial"),
    SMOOTHLEFT("smoothleft"),
    SMOOTHRIGHT("smoothright"),
    SMOOTHUP("smoothup"),
    SMOOTHDOWN("smoothdown"),
    CIRCLEOPEN("circleopen"),
    CIRCLECLOSE("circleclose"),
    VERTOPEN("vertopen"),
    VERTCLOSE("vertclose"),
    HORZOPEN("horzopen"),
    HORZCLOSE("horzclose"),
    PIXELIZE("pixelize"),
    DIAGTL("diagtl"),
    DIAGTR("diagtr"),
    DIAGBL("diagbl"),
    DIAGBR("diagbr"),
    HLSLICE("hlslice"),
    HRSLICE("hrslice"),
    VUSLICE("vuslice"),
    VDSLICE("vdslice"),
    HBLUR("hblur"),
    FADEGRAYS("fadegrays"),
    WIPETL("wipetl"),
    WIPETR("wipetr"),
    WIPEBL("wipebl"),
    WIPEBR("wipebr"),
    SQUEEZEH("squeezeh"),
    SQUEEZEV("squeezev");

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
