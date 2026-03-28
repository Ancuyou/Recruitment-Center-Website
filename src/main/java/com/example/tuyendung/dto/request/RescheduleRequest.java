package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO cho D14 PATCH /api/interviews/{id}/reschedule.
 *
 * ISP: Chỉ chứa field liên quan đến việc dời lịch (thời gian + địa điểm mới).
 * Tách riêng với LichPhongVanRequest để tránh client gửi nhầm tieuDeVong/hinhThuc
 * mà bị ignore silently – gây confusion.
 */
@Data
public class RescheduleRequest {

    @NotNull(message = "Thời gian bắt đầu mới không được để trống")
    @Future(message = "Thời gian bắt đầu phải ở tương lai")
    private LocalDateTime thoiGianBatDau;

    @NotNull(message = "Thời gian kết thúc mới không được để trống")
    @Future(message = "Thời gian kết thúc phải ở tương lai")
    private LocalDateTime thoiGianKetThuc;

    /** Địa điểm hoặc link mới (nullable – nếu null giữ nguyên địa điểm cũ). */
    private String diaDiemHoacLink;
}
