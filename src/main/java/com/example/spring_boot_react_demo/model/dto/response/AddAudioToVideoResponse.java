package com.example.spring_boot_react_demo.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddAudioToVideoResponse {
    Long videoId;
    String asset;
}
