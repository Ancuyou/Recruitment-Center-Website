package com.example.tuyendung.dto.request;

import com.example.tuyendung.entity.enums.HinhThucPhongVan;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LichPhongVanRequest {

    // CHỉ cần thiết khi tạo mới (POST)
    private Long donUngTuyenId;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String tieuDeVong;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @Future(message = "Thời gian bắt đầu phải ở tương lai")
    private LocalDateTime thoiGianBatDau;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @Future(message = "Thời gian kết thúc phải ở tương lai")
    private LocalDateTime thoiGianKetThuc;

    @NotNull(message = "Hình thức phỏng vấn không được để trống")
    private HinhThucPhongVan hinhThuc;

    private String diaDiemHoacLink;
}
