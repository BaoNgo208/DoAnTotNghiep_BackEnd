package com.example.spring_boot_react_demo.service.impl;

import com.cloudinary.Cloudinary;
import com.example.spring_boot_react_demo.model.File;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
//    @Value("${cloudinary.file_upload_URL}")
    public  String cloudinaryUploadURL = "https://res.cloudinary.com/duli95mss/";
    @Resource
    private Cloudinary cloudinary;
    private static final String UPLOAD_ENDPOINT = "/upload/v1/";

    @Override
    public String uploadFile(MultipartFile file, String folderName, String resourceType) {
        try {
            HashMap<Object, Object> options = new HashMap<>();
            options.put("folder", folderName);
            options.put("resource_type", resourceType);
            Map uploadedFile = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploadedFile.get("public_id");
            if (File.VIDEO.name().equals(resourceType)) {
                return cloudinaryUploadURL +File.VIDEO.name()+ cloudinaryUploadURL + publicId + ".mp4";
            } else if (File.AUDIO.name().equals(resourceType)) {
                return cloudinaryUploadURL +File.AUDIO.name()+ cloudinaryUploadURL + publicId + ".mp3";
            } else if (File.IMAGE.name().equals(resourceType)) {
                return cloudinaryUploadURL +File.IMAGE.name()+ cloudinaryUploadURL + publicId + ".jpg";
            }
            return cloudinary.url().secure(true).generate(publicId);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
            return cloudinaryUploadURL + publicId + extension;
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
            Map result = cloudinary.uploader().destroy(extractPublicId(fileUrl,resourceType), options);
            String status = (String) result.get("result");

            return "ok".equals(status);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public String extractPublicId(String fileUrl, String resourceType) {
        String fileUploadURL2 = UPLOAD_ENDPOINT;
        String prefix = cloudinaryUploadURL + resourceType + fileUploadURL2;

        if (fileUrl.startsWith(prefix)) {
            String fileName = fileUrl.substring(prefix.length());
            int lastDotIndex = fileName.lastIndexOf(".");

            if (lastDotIndex != -1) {
                return fileName.substring(0, lastDotIndex);
            }
        }
        return null;
    }
}