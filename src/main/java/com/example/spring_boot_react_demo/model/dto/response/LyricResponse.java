package com.example.spring_boot_react_demo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LyricResponse {

    private Long id;
    private String text;
    private Long projectId;
    private boolean isLyricHidden;
    private String videoUrl;

    public LyricResponse(Long id, String text, Long projectId, boolean isLyricHidden) {
        this.id = id;
        this.text = text;
        this.projectId = projectId;
        this.isLyricHidden = isLyricHidden;
    }
}