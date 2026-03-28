package com.example.tuyendung.entity.enums;

/**
 * Enum cho loại bản ghi trong chi tiết CV
 *
 * SOLID - Open/Closed Principle:
 * - Thêm loại mới chỉ cần thêm constant, không sửa code logic
 * - Tránh magic number (1, 2, 3) rải rác trong code
 */
public enum LoaiBanGhi {

    HOC_VAN(1, "Học vấn"),
    KINH_NGHIEM(2, "Kinh nghiệm làm việc"),
    CHUNG_CHI(3, "Chứng chỉ");

    private final int value;
    private final String label;

    LoaiBanGhi(int value, String label) {
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
     * Tìm LoaiBanGhi từ giá trị integer
     * @throws IllegalArgumentException nếu giá trị không hợp lệ
     */
    public static LoaiBanGhi fromValue(int value) {
        for (LoaiBanGhi loai : values()) {
            if (loai.value == value) {
                return loai;
            }
        }
        throw new IllegalArgumentException("Loại bản ghi không hợp lệ: " + value + ". Chỉ chấp nhận 1 (Học vấn), 2 (Kinh nghiệm), 3 (Chứng chỉ)");
    }

    /**
     * Kiểm tra giá trị integer có hợp lệ không
     */
    public static boolean isValid(int value) {
        for (LoaiBanGhi loai : values()) {
            if (loai.value == value) {
                return true;
            }
        }
        return false;
    }
}
