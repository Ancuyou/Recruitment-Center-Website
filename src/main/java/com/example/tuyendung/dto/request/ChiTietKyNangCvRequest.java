package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietKyNangCvRequest {

    @NotNull(message = "ID kỹ năng không được để trống")
    private Long kyNangId;

    @NotNull(message = "Mức thành thạo không được để trống")
    @Min(value = 1, message = "Mức thành thạo phải từ 1 đến 5")
    @Max(value = 5, message = "Mức thành thạo phải từ 1 đến 5")
    private Integer mucThanhThao; // 1-5: Cơ bản, Trung bình, Tốt, Rất tốt, Chuyên gia
}
