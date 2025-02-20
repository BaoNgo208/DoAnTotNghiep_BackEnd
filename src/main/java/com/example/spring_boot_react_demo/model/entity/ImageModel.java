package com.example.spring_boot_react_demo.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageModel {
    @Id
    private Integer id;
    private String name;
    private MultipartFile file;
}