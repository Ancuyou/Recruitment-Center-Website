package com.example.tuyendung.exception;

import com.example.tuyendung.common.HttpStatusCode;

/**
 * ValidationException – Ném khi dữ liệu input không valid
 * HTTP Status: 400 Bad Request
 *
 * SOLID - SRP: Class chỉ xử lý lỗi validation
 * 
 * Khác với MethodArgumentNotValidException:
 * - Cái này là cho business logic validation
 * - MethodArgumentNotValidException là cho @Valid annotation
 * 
 * Ví dụ:
 * - Mức lương từ > mức lương đến
 * - Ngày bắt đầu > ngày kết thúc
 * - Số year experience không hợp lệ
 */
public class ValidationException extends ApplicationException {
    
    public ValidationException(String message) {
        super(HttpStatusCode.BAD_REQUEST, message);
    }
    
    public ValidationException(String fieldName, String reason) {
        super(HttpStatusCode.BAD_REQUEST, 
              "Trường '" + fieldName + "' không hợp lệ: " + reason);
    }
}
