package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.enums.FileFormat;
import com.example.spring_boot_react_demo.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {
    public boolean isValidFileFormat(MultipartFile file) {
        return file != null && FileFormat.isValidFormat(file.getContentType());
    }
}