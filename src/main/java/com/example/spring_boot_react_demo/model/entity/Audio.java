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
@Table(name = "audio")
public class Audio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="audio_id", nullable = false)
    Long id;

    @Column(name = "audio_asset", nullable = false)
    String asset;

    @Column(name = "audio_type")
    String type;

    @Column(name = "audio_upload_time")
    LocalDateTime uploadTime = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    Project project;

    @Column(name = "start_time")
    Double startTime;

    @Column(name = "end_time")
    Double endTime;
}