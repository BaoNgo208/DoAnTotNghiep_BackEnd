package com.example.spring_boot_react_demo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Getter
@Setter
@Table(name = "lyric")
public class Lyric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="lyric_id", nullable = false)
    Long id;

    @Column(name = "lyric_text", nullable = false ,columnDefinition = "TEXT")
    String text;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    @JsonIgnore
    Project project;

    @Column(name = "is_lyric_hidden", nullable = false)
    boolean isLyricHidden;

    @Column(name = "original_lyric_text" ,columnDefinition = "TEXT")
    private String originalText;

    @Column(name = "effect_lyric_text",columnDefinition = "TEXT")
    private String effectText;
}