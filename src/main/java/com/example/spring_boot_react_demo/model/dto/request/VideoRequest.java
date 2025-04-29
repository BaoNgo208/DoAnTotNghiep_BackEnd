package com.example.spring_boot_react_demo.model.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoRequest {
    Long id;
    Long projectId;
    String asset;
    String assetWithBackground;
    Double duration;
    LocalDateTime uploadTime;
    Double startTime;
    Double endTime;
}
