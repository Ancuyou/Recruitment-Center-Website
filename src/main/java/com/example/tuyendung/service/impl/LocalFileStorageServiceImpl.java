package com.example.tuyendung.service.impl;

import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
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
 * Service thực hiện upload / xóa file cục bộ.
 * Có thể swap với AWS S3, Google Cloud Storage, v.v. nhờ interface FileStorageService.
 *
 * SOLID – OCP: Chỉ phụ thuộc interface, đổi provider không ảnh hưởng caller.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final TimeProvider timeProvider;

    private static final String UPLOAD_DIR = "uploads/cv-files/";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final byte[] PDF_MAGIC = {
            (byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46 // %PDF
    };

    @Override
    public String uploadFile(byte[] fileContent, String fileName, String contentType) {
        log.info("Upload file: {}", fileName);

        // ── Validation ────────────────────────────────────────────────────────
        if (fileContent == null || fileContent.length == 0) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR,
                    "Nội dung file không được để trống");
        }

        if (!"application/pdf".equals(contentType)) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR,
                    "Chỉ hỗ trợ định dạng PDF");
        }

        if (fileContent.length > MAX_FILE_SIZE) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR,
                    "Kích thước file vượt quá giới hạn 10 MB");
        }

        if (!isValidPdfFile(fileContent)) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR,
                    "File không phải là PDF hợp lệ");
        }

        // ── Lưu file ─────────────────────────────────────────────────────────
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            String newFileName = UUID.randomUUID() + "-" + timeProvider.getCurrentTimeMillis() + ".pdf";
            Path filePath = uploadPath.resolve(newFileName);

            Files.write(filePath, fileContent);
            log.info("Upload file thành công: {}", newFileName);

            return "/uploads/cv-files/" + newFileName;
        } catch (SecurityException e) {
            log.error("Lỗi bảo mật khi ghi file", e);
            throw new BaseBusinessException(ErrorCode.FILE_SECURITY_ERROR);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi upload file: {}", fileName, e);
            throw new BaseBusinessException(ErrorCode.FILE_STORAGE_ERROR,
                    "Lỗi upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return;
        }

        log.info("Xóa file: {}", fileUrl);

        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(UPLOAD_DIR, fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Xóa file thành công: {}", fileName);
            } else {
                log.warn("File không tồn tại trên disk, bỏ qua: {}", fileName);
            }
        } catch (SecurityException e) {
            // Không throw – tránh làm gián đoạn luồng chính (ví dụ: xóa CV)
            log.error("Lỗi bảo mật khi xóa file: {}", fileUrl, e);
        } catch (Exception e) {
            // Không throw – xóa file thất bại không nên làm fail toàn bộ operation
            log.error("Lỗi không xác định khi xóa file: {}", fileUrl, e);
        }
    }

    @Override
    public boolean isValidPdfFile(byte[] fileContent) {
        if (fileContent == null || fileContent.length < 4) {
            return false;
        }
        // Kiểm tra 4 byte đầu = magic bytes của PDF (%PDF)
        return fileContent[0] == PDF_MAGIC[0]
                && fileContent[1] == PDF_MAGIC[1]
                && fileContent[2] == PDF_MAGIC[2]
                && fileContent[3] == PDF_MAGIC[3];
    }
}
