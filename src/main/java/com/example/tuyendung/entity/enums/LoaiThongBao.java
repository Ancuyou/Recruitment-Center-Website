package com.example.tuyendung.entity.enums;

/**
 * Enum loại thông báo – Module D15.
 *
 * SOLID - OCP: Thêm loại thông báo mới → thêm constant, không sửa class khác
 * Design Pattern: Enum strategy cho phân loại notification
 *
 * Expand từ 3 giá trị sang 10 giá trị để cover tất cả notification types:
 * - Ứng tuyển: tạo mới, từ chối, offer
 * - Phỏng vấn: tạo, cập nhật, hủy, nhắc nhở
 * - Hệ thống: general
 */
public enum LoaiThongBao {
    // === Ứng tuyển ===
    UNG_TUYEN_TAO_MOI("Ứng viên nộp đơn"),
    UNG_TUYEN_TU_CHOI("Đơn bị từ chối"),
    UNG_TUYEN_VE_PHONG_VAN("Bước vào vòng phỏng vấn"),
    UNG_TUYEN_OFFER("Nhận offer"),

    // === Phỏng vấn ===
    PHONG_VAN_TAO_MOI("Tạo lịch phỏng vấn"),
    PHONG_VAN_CAP_NHAT("Cập nhật lịch phỏng vấn"),
    PHONG_VAN_HUY("Hủy lịch phỏng vấn"),
    PHONG_VAN_REMINDER("Nhắc nhở phỏng vấn"),

    // === Hệ thống ===
    HE_THONG("Thông báo hệ thống");

    private final String label;

    LoaiThongBao(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
