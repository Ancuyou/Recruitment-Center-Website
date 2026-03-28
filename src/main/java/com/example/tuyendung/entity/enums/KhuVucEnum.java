package com.example.tuyendung.entity.enums;

/**
 * Danh sách khu vực có sẵn (predefined, không cần bảng riêng)
 *
 * OCP: Thêm khu vực mới chỉ cần thêm giá trị enum, không sửa logic
 * Lưu dưới DB dạng String (EnumType.STRING) trong bảng ct_kv_tin
 */
public enum KhuVucEnum {

    HA_NOI("Hà Nội"),
    HO_CHI_MINH("TP. Hồ Chí Minh"),
    DA_NANG("Đà Nẵng"),
    CAN_THO("Cần Thơ"),
    HAI_PHONG("Hải Phòng"),
    BINH_DUONG("Bình Dương"),
    DONG_NAI("Đồng Nai"),
    HUNG_YEN("Hưng Yên"),
    BAC_NINH("Bắc Ninh"),
    QUANG_NINH("Quảng Ninh"),
    NGHE_AN("Nghệ An"),
    THUA_THIEN_HUE("Thừa Thiên Huế"),
    KHANH_HOA("Khánh Hòa"),
    LAM_DONG("Lâm Đồng"),
    AN_GIANG("An Giang"),
    REMOTE("Làm việc từ xa");

    private final String label;

    KhuVucEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
