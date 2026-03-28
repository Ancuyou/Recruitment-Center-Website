package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Token không hợp lệ")
    private String token;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    private String newPassword;
}
