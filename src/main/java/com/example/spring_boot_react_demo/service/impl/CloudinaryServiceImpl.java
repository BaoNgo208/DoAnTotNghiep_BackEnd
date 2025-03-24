package com.example.spring_boot_react_demo.service.impl;

import com.cloudinary.Cloudinary;
import com.example.spring_boot_react_demo.model.MediaType;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import static com.example.spring_boot_react_demo.util.Constants.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    @Resource
    private Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file, String resourceType) {
        try {
            HashMap<Object, Object> options = new HashMap<>();
            options.put("resource_type", resourceType);
            Map uploadedFile = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploadedFile.get("public_id");
            if (MediaType.VIDEO.getname().toLowerCase().equals(resourceType)) {
                return CLOUDINARY_UPLOAD_URL + MediaType.VIDEO.getname().toLowerCase() + LOCAL_UPLOAD_URL + publicId + getFileExtension(file);
            } else if (MediaType.AUDIO.getname().equals(resourceType)) {
                return CLOUDINARY_UPLOAD_URL + MediaType.AUDIO.getname().toLowerCase() + LOCAL_UPLOAD_URL + publicId + ".mp3";
            } else if (MediaType.IMAGE.getname().equals(resourceType)) {
                return CLOUDINARY_UPLOAD_URL + MediaType.IMAGE.getname().toLowerCase() + LOCAL_UPLOAD_URL + publicId + ".jpg";
            }
            return cloudinary.url().secure(true).generate(publicId);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteFile(String fileUrl, String resourceType) {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", resourceType);
            String publicId = extractPublicId(fileUrl);
            cloudinary.uploader().destroy(publicId, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getFileExtension(MultipartFile file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf(DOT);
        if (lastDotIndex == NOT_FOUND || lastDotIndex == fileName.length() - ONE) {
            return "";
        }
        return DOT+ fileName.substring(lastDotIndex + ONE);
    }
    private String extractPublicId(String fileUrl) {
        String[] parts = fileUrl.split("/");
        String fileName = parts[parts.length - 1];

        int lastDotIndex = fileName.lastIndexOf(DOT);
        if (lastDotIndex != NOT_FOUND) {
            return fileName.substring(ZERO, lastDotIndex);
        }
        return fileName;
    }
}