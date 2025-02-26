package com.example.spring_boot_react_demo.repository;

import com.example.spring_boot_react_demo.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepo extends JpaRepository<Project, Long> {
}