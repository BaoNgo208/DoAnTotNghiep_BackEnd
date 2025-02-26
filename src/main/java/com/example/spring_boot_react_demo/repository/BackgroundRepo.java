package com.example.spring_boot_react_demo.repository;

import com.example.spring_boot_react_demo.model.entity.Background;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BackgroundRepo extends JpaRepository<Background, Long> {
}