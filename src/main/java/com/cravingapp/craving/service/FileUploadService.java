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
    public void deleteFile(String url) {
        // Dacă nu ai salvat public_id în bază, îl extragem din URL
        String publicId = extractPublicIdFromUrl(url);

        try {
            // "destroy" este comanda pentru ștergere în Cloudinary
            // Includem "invalidate: true" pentru a șterge și din cache-ul CDN
            Map params = ObjectUtils.asMap("invalidate", true);

            cloudinary.uploader().destroy(publicId, params);

        } catch (IOException e) {
            throw new RuntimeException("Eroare la ștergerea imaginii de pe Cloudinary: " + publicId, e);
        }
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) return "";

        try {
            // Găsim ultima parte a URL-ului (după ultimul slash /)
            int lastSlashIndex = url.lastIndexOf("/");
            String filename = url.substring(lastSlashIndex + 1);

            // Găsim extensia (.jpg, .png)
            int dotIndex = filename.lastIndexOf(".");

            if (dotIndex == -1) {
                return filename; // Nu are extensie
            }

            // Returnăm numele fără extensie (acesta este de obicei public_id-ul simplu)
            // Notă: Dacă folosești foldere în Cloudinary, logica e puțin mai complexă,
            // dar pentru upload-uri simple, asta e suficient.
            return filename.substring(0, dotIndex);
        } catch (Exception e) {
            System.err.println("Nu s-a putut extrage public_id din URL: " + url);
            return "";
        }
    }
}