package com.example.tuyendung.entity.enums;

/**
 * Danh sách ngành nghề có sẵn (predefined, không cần bảng riêng)
 *
 * OCP: Thêm ngành mới chỉ cần thêm giá trị enum, không sửa logic
 * Lưu dưới DB dạng String (EnumType.STRING) trong bảng ct_cty_nganh
 */
public enum NganhNgheEnum {

    CONG_NGHE_THONG_TIN("Công nghệ thông tin"),
    TAI_CHINH_NGAN_HANG("Tài chính - Ngân hàng"),
    BAN_LE_THUONG_MAI_DIEN_TU("Bán lẻ - Thương mại điện tử"),
    SAN_XUAT_KY_THUAT("Sản xuất - Kỹ thuật"),
    Y_TE_DUOC_PHAM("Y tế - Dược phẩm"),
    GIAO_DUC_DAO_TAO("Giáo dục - Đào tạo"),
    MARKETING_TRUYEN_THONG("Marketing - Truyền thông"),
    XAY_DUNG_BAT_DONG_SAN("Xây dựng - Bất động sản"),
    NHA_HANG_KHACH_SAN("Nhà hàng - Khách sạn"),
    LOGISTICS_VAN_TAI("Logistics - Vận tải"),
    NONG_NGHIEP("Nông nghiệp"),
    NANG_LUONG("Năng lượng"),
    BAO_HIEM("Bảo hiểm"),
    LAM_DEP_SPA("Làm đẹp - Spa"),
    KHAC("Khác");

    private final String label;

    NganhNgheEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
