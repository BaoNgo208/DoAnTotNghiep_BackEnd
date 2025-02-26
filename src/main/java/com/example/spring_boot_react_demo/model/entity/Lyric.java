package com.example.spring_boot_react_demo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "lyric")
public class Lyric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="lyric_id", nullable = false)
    Long id;

    @Column(name = "lyric_text", nullable = false)
    String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    Project project;

    @Column(name = "start_time")
    Double startTime;

    @Column(name = "end_time")
    Double endTime;
}