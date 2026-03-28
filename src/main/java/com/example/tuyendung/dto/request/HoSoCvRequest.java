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
public class HoSoCvRequest {

    @NotBlank(message = "Tiêu đề CV không được để trống")
    @Size(min = 1, max = 100, message = "Tiêu đề CV phải từ 1 đến 100 ký tự")
    private String tieuDeCv;

    @Size(max = 1000, message = "Mục tiêu nghề nghiệp tối đa 1000 ký tự")
    private String mucTieuNgheNghiep;

    @Size(max = 500, message = "URL file tối đa 500 ký tự")
    private String fileCvUrl;
}
