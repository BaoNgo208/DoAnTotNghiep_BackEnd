package com.example.spring_boot_react_demo.repository;

import com.example.spring_boot_react_demo.model.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepo extends JpaRepository<Video,Long> {
}
