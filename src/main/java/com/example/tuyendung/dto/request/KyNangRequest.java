package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KyNangRequest {

    @NotBlank(message = "Tên kỹ năng không được để trống")
    @Size(min = 1, max = 100, message = "Tên kỹ năng phải từ 1 đến 100 ký tự")
    private String tenKyNang;
}
