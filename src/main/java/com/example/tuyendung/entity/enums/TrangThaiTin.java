package com.example.tuyendung.entity.enums;

/**
 * Enum thay thế magic numbers cho trang_thai của TinTuyenDung
 *
 * OCP: Thêm trạng thái mới chỉ cần thêm vào enum, không sửa logic
 * DB lưu dạng Integer để backward compatibility
 */
public enum TrangThaiTin {

    NHAP(0, "Nháp"),
    MO(1, "Đang mở"),
    DONG(2, "Đã đóng");

    private final int value;
    private final String label;

    TrangThaiTin(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Lấy enum từ integer value
     */
    public static TrangThaiTin fromValue(int value) {
        for (TrangThaiTin t : values()) {
            if (t.value == value) return t;
        }
        throw new IllegalArgumentException("Trạng thái tin không hợp lệ: " + value);
    }

    /**
     * Kiểm tra value hợp lệ (0, 1, 2)
     */
    public static boolean isValid(int value) {
        for (TrangThaiTin t : values()) {
            if (t.value == value) return true;
        }
        return false;
    }
}
