package com.example.spring_boot_react_demo.model.dto.response;

import com.example.spring_boot_react_demo.model.entity.Background;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectBasicResponse {
    Long id;
    String name ;
    LocalDateTime uploadTime ;
    Double length ;
    String asset;
    Background background;
}
