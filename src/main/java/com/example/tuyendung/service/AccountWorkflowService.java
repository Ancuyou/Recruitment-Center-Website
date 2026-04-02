package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.ForgotPasswordRequest;
import com.example.tuyendung.dto.request.ResetPasswordRequest;

/**
 * Service chuyên biệt xử lý xác thực tài khoản và luồng khôi phục mật khẩu (SRP Refactoring - H4)
 */
public interface AccountWorkflowService {

    /**
     * Xác thực email cho tài khoản mới đăng ký
     */
    void verifyEmail(String token);

    /**
     * Gửi yêu cầu đặt lại mật khẩu bằng email
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Đặt lại mật khẩu với mã xác thực
     */
    void resetPassword(ResetPasswordRequest request);
}
