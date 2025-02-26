package com.example.spring_boot_react_demo.model.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectResponse {
    Long id;
    String name ;
    LocalDateTime uploadTime ;
    Double length ;
    List<BackgroundResponse> backgrounds;
}