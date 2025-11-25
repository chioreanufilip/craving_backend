package com.cravingapp.craving.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils; // Import important!
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    public Map<String,String> upload(MultipartFile file) {
        try {
            Map params = ObjectUtils.asMap("resource_type", "auto");
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String url = uploadResult.get("secure_url").toString();
            String type = uploadResult.get("resource_type").toString().toUpperCase();
            return Map.of("url", url, "type", type);

        } catch (IOException e) {
            throw new RuntimeException("Eroare la încărcarea imaginii pe Cloudinary", e);
        }
    }
}