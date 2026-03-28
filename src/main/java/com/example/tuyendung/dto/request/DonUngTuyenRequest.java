package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO request cho D1 – Nộp đơn ứng tuyển.
 *
 * SOLID - ISP: Chỉ định nghĩa field cần thiết cho D1
 * Clean Code: Field validate đầy đủ (@NotNull, @Size)
 *
 * OOP - Validation: Bean Validation (Jakarta) thay vì logic trong service
 */
@Data
public class DonUngTuyenRequest {

    @NotNull(message = "ID tin tuyển dụng không được để trống")
    private Long tinTuyenDungId;

    @NotNull(message = "ID hồ sơ CV không được để trống")
    private Long hoSoCvId;

    @Size(max = 2000, message = "Thư ngỏ tối đa 2000 ký tự")
    private String thuNgo;
}
