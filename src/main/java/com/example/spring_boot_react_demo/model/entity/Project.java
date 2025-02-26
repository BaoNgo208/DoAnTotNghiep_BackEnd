package com.example.spring_boot_react_demo.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id", nullable = false)
    Long id;

    @Column(name = "project_name", nullable = false)
    String name = "Untitled Project";

    @Column(name = "project_upload_time")
    LocalDateTime uploadTime = LocalDateTime.now();

    @Column(name ="project_length")
    Double length ;

    @Column(name = "project_asset")
    String asset;

    @OneToMany(mappedBy = "project")
    List<Video> Video = new ArrayList<>();

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Background background;
}