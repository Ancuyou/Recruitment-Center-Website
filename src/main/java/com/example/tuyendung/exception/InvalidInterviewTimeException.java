package com.example.tuyendung.exception;

/**
 * Exception khi thời gian phỏng vấn không hợp lệ.
 * HTTP Status: 400 Bad Request
 *
 * SOLID - SRP: Class chỉ xử lý lỗi thời gian invalid
 */
public class InvalidInterviewTimeException extends BusinessException {
    
    public InvalidInterviewTimeException(String message) {
        super(400, message);
    }
    
    public InvalidInterviewTimeException() {
        super(400, "Thời gian kết thúc phải sau thời gian bắt đầu");
    }
}
