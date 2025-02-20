package com.example.spring_boot_react_demo.service.impl;

import com.cloudinary.Cloudinary;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    public static final String fileUploadURL = "https://res.cloudinary.com/duli95mss/video/upload/v1/";
    @Resource
    private Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file, String folderName, String resourceType) {
        try {
            HashMap<Object, Object> options = new HashMap<>();
            options.put("folder", folderName);
            options.put("resource_type", resourceType);

            Map uploadedFile = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploadedFile.get("public_id");

            if ("video".equals(resourceType)) {
                return fileUploadURL + publicId + ".mp4";
            } else if ("audio".equals(resourceType)) {
                return fileUploadURL + publicId + ".mp3";
            }

            return cloudinary.url().secure(true).generate(publicId);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}