package com.example.tuyendung.exception;

import com.example.tuyendung.common.HttpStatusCode;

/**
 * Exception khi người dùng cố truy cập đơn ứng tuyển không thuộc quyền của họ.
 * HTTP Status: 403 Forbidden
 *
 * SOLID - SRP: Class chỉ xử lý lỗi unauthorized access
 */
public class UnauthorizedApplicationAccessException extends BusinessException {
    
    public UnauthorizedApplicationAccessException() {
        super(HttpStatusCode.FORBIDDEN, "Không có quyền truy cập đơn ứng tuyển này");
    }

    public UnauthorizedApplicationAccessException(Long applicationId) {
        super(HttpStatusCode.FORBIDDEN, "Không có quyền truy cập đơn ID: " + applicationId);
    }
}
