package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.exception.FileStorageException;
import com.smartcampus.erp.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "zip", "png", "jpg", "jpeg");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public FileStorageServiceImpl(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file.");
        }

        // Validate File Size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size exceeds the maximum limit of 5MB.");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Validate File Extension
        String fileExtension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new FileStorageException("File type not allowed. Allowed types are: " + ALLOWED_EXTENSIONS);
        }

        try {
            // Check if the filename contains invalid characters
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Create a unique file name to avoid collision
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf + 1);
    }
}
