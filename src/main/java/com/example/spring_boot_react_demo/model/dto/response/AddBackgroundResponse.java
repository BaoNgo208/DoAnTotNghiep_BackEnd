package com.example.spring_boot_react_demo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddBackgroundResponse {
    Long videoId;
    String asset;
    String assetWithBackground;
}
