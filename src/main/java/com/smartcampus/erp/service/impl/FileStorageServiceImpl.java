package com.smartcampus.erp.service.impl;

import com.smartcampus.erp.exception.FileStorageException;
import com.smartcampus.erp.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String publicUrlPrefix;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("pdf", "zip", "png", "jpg", "jpeg");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public FileStorageServiceImpl(
            S3Client s3Client,
            @Value("${aws.s3.bucket:mock-bucket}") String bucketName,
            @Value("${aws.s3.public-url-prefix:http://localhost:8080/uploads}") String publicUrlPrefix) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.publicUrlPrefix = publicUrlPrefix;
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

        var originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Validate File Extension
        var fileExtension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new FileStorageException("File type not allowed. Allowed types are: " + ALLOWED_EXTENSIONS);
        }

        try {
            // Check if the filename contains invalid characters
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Create a unique file name to avoid collision
            var uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // Stream to AWS S3
            var putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Return a stable cloud URL
            return publicUrlPrefix + "/" + uniqueFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + " to S3. Please try again!", ex);
        } catch (Exception ex) {
            throw new FileStorageException("S3 Storage upload failed for file " + originalFileName, ex);
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
