package com.example.spring_boot_react_demo.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference
    Project project;
}