package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO cho tạo/cập nhật công ty (B1, B4)
 */
@Getter
@Setter
@NoArgsConstructor
public class CongTyRequest {

    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 255, message = "Tên công ty tối đa 255 ký tự")
    private String tenCongTy;

    @Pattern(regexp = "^[0-9]{10,14}$", message = "Mã số thuế phải gồm 10-14 chữ số")
    private String maSoThue;

    @Size(max = 255, message = "URL logo tối đa 255 ký tự")
    private String logoUrl;

    @Size(max = 255, message = "Website tối đa 255 ký tự")
    private String website;

    private String moTa;
}
