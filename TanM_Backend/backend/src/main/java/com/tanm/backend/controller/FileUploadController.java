package com.tanm.backend.controller;

import com.tanm.backend.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "products") String folder
    ) {
        Map<String, Object> result = cloudinaryService.uploadImage(file, folder);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/images")
    public ResponseEntity<java.util.List<Map<String, Object>>> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", required = false, defaultValue = "products") String folder
    ) {
        java.util.List<Map<String, Object>> results = cloudinaryService.uploadImages(files, folder);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/image")
    public ResponseEntity<Map<String, String>> deleteImage(@RequestParam("publicId") String publicId) {
        cloudinaryService.deleteImage(publicId);
        return ResponseEntity.ok(Map.of("message", "Image deleted from Cloudinary successfully"));
    }
}
