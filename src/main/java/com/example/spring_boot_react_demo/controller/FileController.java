package com.example.spring_boot_react_demo.controller;

import com.example.spring_boot_react_demo.service.FileService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/validate/files")
public class FileController {
    @Autowired
    FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return fileService.isValidFileFormat(file)
                ? ResponseEntity.ok("File uploaded successfully.")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file format.");
    }
}