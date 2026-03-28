package com.example.tuyendung.service.impl;

import com.example.tuyendung.service.FileStorageService;
import com.example.tuyendung.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service thực hiện upload file local
 * Có thể swap với AWS S3, Google Cloud Storage, v.v. nhờ interface
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final TimeProvider timeProvider;
    private static final String UPLOAD_DIR = "uploads/cv-files/";
    // private static final String[] ALLOWED_TYPES = {"application/pdf"}; // Reserved for future use
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final byte[] PDF_MAGIC = {(byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46}; // %PDF

    @Override
    public String uploadFile(byte[] fileContent, String fileName, String contentType) {
        log.info("Upload file: {}", fileName);

        // Validate file null
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("Nội dung file không được để trống");
        }

        // Validate file type
        if (!contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("Chỉ hỗ trợ file PDF");
        }

        // Validate file size
        if (fileContent.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file vượt quá giới hạn 10MB");
        }

        // Validate PDF file
        if (!isValidPdfFile(fileContent)) {
            throw new IllegalArgumentException("File không phải là PDF hợp lệ");
        }

        try {
            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            // Tạo tên file unique
            String newFileName = UUID.randomUUID() + "-" + timeProvider.getCurrentTimeMillis() + ".pdf";
            Path filePath = uploadPath.resolve(newFileName);

            // Save file
            Files.write(filePath, fileContent);
            log.info("Upload file thành công: {}", newFileName);

            // Return relative URL
            return "/uploads/cv-files/" + newFileName;
        } catch (SecurityException e) {
            log.error("Lỗi bảo mật upload file", e);
            throw new RuntimeException("Lỗi bảo mật: không có quyền ghi file");
        } catch (Exception e) {
            log.error("Lỗi upload file", e);
            throw new RuntimeException("Lỗi upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return;
        }

        log.info("Xóa file: {}", fileUrl);

        try {
            // Extract file name from URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(UPLOAD_DIR, fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Xóa file thành công: {}", fileName);
            } else {
                log.warn("File không tồn tại: {}", fileName);
            }
        } catch (SecurityException e) {
            log.error("Lỗi bảo mật: không có quyền xóa file", e);
        } catch (Exception e) {
            log.error("Lỗi xóa file: {}", fileUrl, e);
            // Không throw exception để không làm gián đoạn flow chính
            // (Nếu file xóa fail, không nên làm chúng ta operation fail)
        }
    }

    @Override
    public boolean isValidPdfFile(byte[] fileContent) {
        if (fileContent == null || fileContent.length < 4) {
            return false;
        }

        // Check PDF magic bytes (%PDF)
        return fileContent[0] == PDF_MAGIC[0] &&
               fileContent[1] == PDF_MAGIC[1] &&
               fileContent[2] == PDF_MAGIC[2] &&
               fileContent[3] == PDF_MAGIC[3];
    }
}
