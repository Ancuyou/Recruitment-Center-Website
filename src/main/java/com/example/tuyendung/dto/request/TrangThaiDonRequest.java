package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrangThaiDonRequest {

    @NotNull(message = "Trạng thái không được để trống")
    @Min(value = 1, message = "Trạng thái không hợp lệ")
    @Max(value = 5, message = "Trạng thái không hợp lệ")
    private Integer trangThaiMoi;

    private String ghiChu;
}
