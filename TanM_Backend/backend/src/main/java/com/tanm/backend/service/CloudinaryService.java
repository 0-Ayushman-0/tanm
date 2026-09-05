package com.tanm.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tanm.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files (JPG, PNG, WEBP, GIF, SVG) are supported");
        }

        try {
            String uploadFolder = (folder != null && !folder.isBlank()) ? "tanm/" + folder.trim() : "tanm/products";
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", uploadFolder,
                    "overwrite", true,
                    "resource_type", "auto"
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return Map.of(
                    "url", String.valueOf(uploadResult.get("secure_url")),
                    "publicId", String.valueOf(uploadResult.get("public_id")),
                    "format", String.valueOf(uploadResult.get("format")),
                    "width", uploadResult.get("width") != null ? uploadResult.get("width") : 0,
                    "height", uploadResult.get("height") != null ? uploadResult.get("height") : 0,
                    "bytes", uploadResult.get("bytes") != null ? uploadResult.get("bytes") : 0
            );
        } catch (IOException e) {
            throw new BadRequestException("Cloudinary upload failed: " + e.getMessage());
        }
    }

    public java.util.List<Map<String, Object>> uploadImages(MultipartFile[] files, String folder) {
        if (files == null || files.length == 0) {
            throw new BadRequestException("Please select at least one file to upload");
        }
        return java.util.Arrays.stream(files)
                .map(file -> uploadImage(file, folder))
                .collect(java.util.stream.Collectors.toList());
    }

    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new BadRequestException("Cloudinary deletion failed: " + e.getMessage());
        }
    }
}
