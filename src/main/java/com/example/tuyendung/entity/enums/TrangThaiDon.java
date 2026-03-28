package com.example.tuyendung.entity.enums;

/**
 * Enum cho trạng thái đơn ứng tuyển (DonUngTuyen.trangThaiHienTai)
 *
 * OCP: Thêm trạng thái mới chỉ cần thêm constant
 * Thay magic numbers 1/2/3/4/5 trong B22 statistics
 */
public enum TrangThaiDon {

    MOI(1, "Mới"),
    REVIEW(2, "Đang xem xét"),
    PHONG_VAN(3, "Phỏng vấn"),
    OFFER(4, "Đã offer"),
    TU_CHOI(5, "Từ chối");

    private final int value;
    private final String label;

    TrangThaiDon(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static TrangThaiDon fromValue(int value) {
        for (TrangThaiDon t : values()) {
            if (t.value == value) return t;
        }
        throw new IllegalArgumentException("Trạng thái đơn không hợp lệ: " + value);
    }
}
