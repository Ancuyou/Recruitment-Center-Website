package com.example.tuyendung.exception;

import com.example.tuyendung.common.HttpStatusCode;

/**
 * UnauthorizedException – Ném khi không có quyền truy cập resource
 * HTTP Status: 403 Forbidden
 *
 * SOLID - SRP: Class chỉ xử lý lỗi authorization
 * 
 * Lưu ý: Khác với Authentication (401), đây là Authorization (403)
 * - 401 Unauthorized: User chưa login hoặc token invalid
 * - 403 Forbidden: User đã login nhưng không có quyền
 * 
 * Ví dụ:
 * - Ứng viên cố xem đơn của người khác
 * - HR cố xóa tin đăng có ứng dụng
 * - Admin action trên resource ko phải của mình
 */
public class UnauthorizedException extends ApplicationException {
    
    public UnauthorizedException(String message) {
        super(HttpStatusCode.FORBIDDEN, message);
    }
    
    public UnauthorizedException(String resourceType, Long resourceId) {
        super(HttpStatusCode.FORBIDDEN, 
              "Bạn không có quyền truy cập " + resourceType + " ID " + resourceId);
    }
    
    public UnauthorizedException(String action, String resourceType) {
        super(HttpStatusCode.FORBIDDEN, 
              "Bạn không có quyền " + action + " " + resourceType);
    }
}
