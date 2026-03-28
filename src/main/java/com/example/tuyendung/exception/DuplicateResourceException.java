package com.example.tuyendung.exception;

import com.example.tuyendung.common.HttpStatusCode;

/**
 * DuplicateResourceException – Ném khi cố tạo resource trùng lặp
 * HTTP Status: 409 Conflict
 *
 * SOLID - SRP: Class chỉ xử lý lỗi duplicate resource
 * 
 * Ví dụ:
 * - Nộp đơn 2 lần cho cùng job
 * - Email đã được đăng ký
 * - Skill đã tồn tại trong CV
 */
public class DuplicateResourceException extends ApplicationException {
    
    public DuplicateResourceException(String message) {
        super(HttpStatusCode.CONFLICT, message);
    }
    
    public DuplicateResourceException(String resourceType, String identifier) {
        super(HttpStatusCode.CONFLICT, 
              resourceType + " '" + identifier + "' đã tồn tại");
    }
    
    public DuplicateResourceException(String action, Long resource1Id, Long resource2Id) {
        super(HttpStatusCode.CONFLICT, 
              "Không thể " + action + ": Resource ID " + resource1Id + 
              " và ID " + resource2Id + " đã bị trùng lặp");
    }
}
