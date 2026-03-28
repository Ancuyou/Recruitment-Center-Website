package com.example.tuyendung.service;

import com.example.tuyendung.dto.response.ThongBaoResponse;
import com.example.tuyendung.entity.enums.LoaiThongBao;
import org.springframework.data.domain.Page;

/**
 * Service interface quản lý thông báo – Module D15-D18.
 *
 * ISP (Interface Segregation): chỉ khai báo các method thực sự cần cho module D.
 * OCP (Open/Closed): thêm loại thông báo mới → chỉ cần mở rộng LoaiThongBao enum,
 *   không cần sửa interface này.
 */
public interface ThongBaoService {

    /**
     * Tạo một thông báo mới gửi đến tài khoản chỉ định.
     *
     * @param taiKhoanId  ID tài khoản người nhận
     * @param tieuDe      Tiêu đề thông báo
     * @param noiDung     Nội dung thông báo
     * @param loai        Loại thông báo (UNG_TUYEN, PHONG_VAN, HE_THONG)
     * @param lienKet     Đường dẫn liên kết (nullable)
     */
    void createNotification(Long taiKhoanId, String tieuDe, String noiDung,
                            LoaiThongBao loai, String lienKet);

    /** D15: Danh sách thông báo của tài khoản, phân trang, mới nhất trước. */
    Page<ThongBaoResponse> getMyNotifications(Long taiKhoanId, int page, int size);

    /** D17: Đếm số thông báo chưa đọc. */
    long countUnread(Long taiKhoanId);

    /** D16: Đánh dấu một thông báo là đã đọc – kiểm tra quyền sở hữu. */
    void markAsRead(Long id, Long taiKhoanId);

    /** D18: Đánh dấu TẤT CẢ thông báo chưa đọc của tài khoản là đã đọc (bulk update). */
    void markAllAsRead(Long taiKhoanId);
}
