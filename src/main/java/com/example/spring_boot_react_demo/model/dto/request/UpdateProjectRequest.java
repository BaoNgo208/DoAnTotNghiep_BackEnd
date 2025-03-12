package com.example.spring_boot_react_demo.model.dto.request;

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
public class UpdateProjectRequest {
    Long id;
    String name;
    LocalDateTime uploadTime;
    Double length;
    String size;
    String asset;
    List<VideoRequest> videos;
    Background background;
}