package com.example.spring_boot_react_demo.model.dto.response;

import com.example.spring_boot_react_demo.model.entity.Background;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectFullResponse {
    Long id;
    String name;
    LocalDateTime uploadTime;
    Double length;
    String size;
    String asset;
    List<VideoResponse> videos;
    Background background;
}