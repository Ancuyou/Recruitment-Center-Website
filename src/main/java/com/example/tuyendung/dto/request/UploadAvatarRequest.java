package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UploadAvatarRequest {
    @NotBlank(message = "URL hoặc link ảnh không được để trống")
    private String avatarUrl;
}
