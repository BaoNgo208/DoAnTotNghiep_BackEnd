package com.example.spring_boot_react_demo.repository;

import com.example.spring_boot_react_demo.model.entity.Audio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioRepo extends JpaRepository<Audio, Long> {
}