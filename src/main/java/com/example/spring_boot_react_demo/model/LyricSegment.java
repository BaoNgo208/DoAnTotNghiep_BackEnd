package com.example.spring_boot_react_demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LyricSegment {
    String text;
    double startTime;
    double endTime;

    public LyricSegment(String text, double startTime, double endTime) {
        this.text = text;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
