package com.example.tuyendung.exception;

import com.example.tuyendung.common.HttpStatusCode;

/**
 * Exception khi không tìm thấy đơn ứng tuyển.
 * HTTP Status: 404 Not Found
 *
 * SOLID - SRP: Class chỉ xử lý một trách nhiệm duy nhất
 * (báo cáo lỗi không tìm thấy application)
 */
public class ApplicationNotFoundException extends BusinessException {
    
    public ApplicationNotFoundException(Long id) {
        super(HttpStatusCode.NOT_FOUND, "Không tìm thấy đơn ứng tuyển ID: " + id);
    }
}
