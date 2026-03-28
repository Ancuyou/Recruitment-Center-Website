package com.example.tuyendung.exception;

/**
 * Exception khi không tìm thấy lịch phỏng vấn.
 * HTTP Status: 404 Not Found
 *
 * SOLID - SRP: Class chỉ xử lý lỗi không tìm thấy interview
 */
public class InterviewNotFoundException extends BusinessException {
    
    public InterviewNotFoundException(Long id) {
        super(404, "Không tìm thấy lịch phỏng vấn ID: " + id);
    }
}
