package com.example.tuyendung.dto.request;

import com.example.tuyendung.entity.enums.CapBacYeuCau;
import com.example.tuyendung.entity.enums.HinhThucLamViec;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO cho tạo/cập nhật tin tuyển dụng (B13, B16)
 */
@Getter
@Setter
@NoArgsConstructor
public class TinTuyenDungRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String tieuDe;

    @NotBlank(message = "Mô tả công việc không được để trống")
    private String moTaCongViec;

    @NotBlank(message = "Yêu cầu ứng viên không được để trống")
    private String yeuCauUngVien;

    @Positive(message = "Mức lương min phải > 0")
    private BigDecimal mucLuongMin;

    @Positive(message = "Mức lương max phải > 0")
    private BigDecimal mucLuongMax;

    @Size(max = 100, message = "Địa điểm tối đa 100 ký tự")
    private String diaDiem;

    @NotNull(message = "Cấp bậc yêu cầu không được để trống")
    private CapBacYeuCau capBacYeuCau;

    @NotNull(message = "Hình thức làm việc không được để trống")
    private HinhThucLamViec hinhThucLamViec;

    @NotNull(message = "Hạn nộp không được để trống")
    @Future(message = "Hạn nộp phải là ngày trong tương lai")
    private LocalDate hanNop;
}
