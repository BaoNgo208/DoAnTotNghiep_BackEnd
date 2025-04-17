package com.example.spring_boot_react_demo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IndexedVideoResponse {
    private int index;
    private VideoResponse response;
}