package com.example.tuyendung.service;

/**
 * Service interface cho file storage (upload PDF CV)
 * Tuân thủ SOLID: Single Responsibility, Open/Closed (có thể swap implementation)
 */
public interface FileStorageService {

    /**
     * Upload file và trả về URL
     */
    String uploadFile(byte[] fileContent, String fileName, String contentType);

    /**
     * Xóa file
     */
    void deleteFile(String fileUrl);

    /**
     * Kiểm tra file có hợp lệ không
     */
    boolean isValidPdfFile(byte[] fileContent);
}
