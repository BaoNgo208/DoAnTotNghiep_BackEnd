package com.example.spring_boot_react_demo.repository;

import com.example.spring_boot_react_demo.model.entity.Lyric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LyricRepo extends JpaRepository<Lyric, Long> {
}