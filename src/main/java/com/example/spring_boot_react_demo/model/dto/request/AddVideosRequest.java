package com.example.spring_boot_react_demo.model.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddVideosRequest {
    Long projectId;
    List<VideoRequest> videoRequestList;
}
