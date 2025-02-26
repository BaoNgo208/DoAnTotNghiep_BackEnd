package com.example.spring_boot_react_demo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "video")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="video_id", nullable = false)
    Long id;

    @Column(name = "video_asset", nullable = false)
    String asset;

    @Column(name = "video_upload_time")
    LocalDateTime uploadTime = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    Project project;

    @Column(name = "start_time")
    Double startTime;

    @Column(name = "end_time")
    Double endTime;
}