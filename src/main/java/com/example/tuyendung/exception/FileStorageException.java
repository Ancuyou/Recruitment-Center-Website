package com.example.tuyendung.exception;

import com.example.tuyendung.common.HttpStatusCode;

/**
 * FileStorageException – Ném khi có lỗi xử lý file
 * HTTP Status: 400 Bad Request (hoặc 500 nếu server error)
 *
 * SOLID - SRP: Class chỉ xử lý lỗi file storage
 * 
 * Ví dụ:
 * - File size vượt quá limit
 * - File type không được phép
 * - Lỗi khi lưu file
 * - Lỗi khi xóa file
 */
public class FileStorageException extends ApplicationException {
    
    public FileStorageException(String message) {
        super(HttpStatusCode.BAD_REQUEST, message);
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(HttpStatusCode.BAD_REQUEST, message, cause);
    }
    
    public FileStorageException(int httpStatusCode, String message) {
        super(httpStatusCode, message);
    }
    
    public FileStorageException(int httpStatusCode, String message, Throwable cause) {
        super(httpStatusCode, message, cause);
    }
}
