package com.example.tuyendung.exception;

import com.example.tuyendung.common.HttpStatusCode;

/**
 * ResourceNotFoundException – Ném khi tìm kiếm entity không tìm thấy.
 * HTTP Status: 404 Not Found
 *
 * SOLID - SRP: Class chỉ xử lý lỗi resource not found
 * 
 * Ví dụ:
 * - Tìm HoSoCV với ID không tồn tại
 * - Tìm CongTy với ID không tồn tại
 * - Tìm TinTuyenDung đã bị xóa
 */
public class ResourceNotFoundException extends ApplicationException {
    
    public ResourceNotFoundException(String message) {
        super(HttpStatusCode.NOT_FOUND, message);
    }
    
    public ResourceNotFoundException(String resourceName, Long id) {
        super(HttpStatusCode.NOT_FOUND, resourceName + " có ID " + id + " không tồn tại");
    }
    
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(HttpStatusCode.NOT_FOUND, resourceName + " '" + identifier + "' không tìm thấy");
    }
}
