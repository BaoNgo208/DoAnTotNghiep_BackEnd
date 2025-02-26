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
@Table(name = "background")
public class Background {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="background_id", nullable = false)
    Long id;

    @Column(name = "background_asset", nullable = false)
    String asset;

    @Column(name = "background_type")
    String type;

    @Column(name = "background_upload_time")
    LocalDateTime uploadTime = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    Project project;

    @Column(name = "background_display_order")
    Integer displayOrder;

    @Column(name = "start_time")
    Double startTime;

    @Column(name = "end_time")
    Double endTime;
}