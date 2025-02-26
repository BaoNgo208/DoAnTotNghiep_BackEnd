package com.example.spring_boot_react_demo.model.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoResponse {
    Long id;
    Long projectId;
    String asset;
    LocalDateTime uploadTime;
    Double startTime;
    Double endTime;
}
