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
    public String uploadFile(MultipartFile file, String folderName, String resourceType) {
        try {
            HashMap<Object, Object> options = new HashMap<>();
            options.put("folder", folderName);
            options.put("resource_type", resourceType);
            Map uploadedFile = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploadedFile.get("public_id");
            if (MediaType.VIDEO.name().toLowerCase().equals(resourceType)) {
                return CLOUDINARY_UPLOAD_URL + MediaType.VIDEO.name().toLowerCase() + LOCAL_UPLOAD_URL + publicId + ".mp4";
            } else if (MediaType.AUDIO.name().equals(resourceType)) {
                return CLOUDINARY_UPLOAD_URL + MediaType.AUDIO.name().toLowerCase() + LOCAL_UPLOAD_URL + publicId + ".mp3";
            } else if (MediaType.IMAGE.name().equals(resourceType)) {
                return CLOUDINARY_UPLOAD_URL + MediaType.IMAGE.name().toLowerCase() + LOCAL_UPLOAD_URL + publicId + ".jpg";
            }
            return cloudinary.url().secure(true).generate(publicId);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public boolean deleteFile(String fileUrl, String resourceType) {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", resourceType);
            Map result = cloudinary.uploader().destroy(extractPublicId(fileUrl, resourceType), options);
            String status = (String) result.get("result");

            return "ok".equals(status);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String extractPublicId(String fileUrl, String resourceType) {
        String prefix = CLOUDINARY_UPLOAD_URL + resourceType + LOCAL_UPLOAD_URL;

        if (fileUrl.startsWith(prefix)) {
            String fileName = fileUrl.substring(prefix.length());
            int lastDotIndex = fileName.lastIndexOf(DOT);
            int noDot = NOT_FOUND;
            if (lastDotIndex != noDot) {
                return fileName.substring(ZERO, lastDotIndex);
            }
        }
        return null;
    }
    @Override
    public String export(MultipartFile file, String folderName, String resourceType) {
        try {
            HashMap<Object, Object> options = new HashMap<>();
            options.put("folder", folderName);
            options.put("resource_type", resourceType);
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            Map uploadedFile = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploadedFile.get("public_id");
            return CLOUDINARY_UPLOAD_URL + MediaType.VIDEO.name() + LOCAL_UPLOAD_URL + publicId + extension;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}