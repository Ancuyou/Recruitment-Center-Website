package com.example.tuyendung.exception;

/**
 * Exception khi ứng viên cố nộp đơn trùng cho cùng một tin tuyển dụng.
 * HTTP Status: 409 Conflict
 *
 * SOLID - SRP: Class chỉ xử lý lỗi duplicate application
 */
public class DuplicateApplicationException extends DuplicateResourceException {
    
    public DuplicateApplicationException(Long tinTuyenDungId, Long taiKhoanId) {
        super("Ứng dụng", "đơn ứng tuyển vào job ID " + tinTuyenDungId);
    }
}
