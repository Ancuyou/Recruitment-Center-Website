package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.LichPhongVanRequest;
import com.example.tuyendung.dto.request.RescheduleRequest;
import com.example.tuyendung.dto.response.LichPhongVanResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface nghiệp vụ lịch phỏng vấn – Module D9-D14.
 *
 * SOLID:
 * - SRP: chỉ khai báo method liên quan đến LichPhongVan.
 * - ISP: không khai báo method không liên quan.
 * - DIP: controller phụ thuộc interface này, không phụ thuộc impl.
 *
 * Tách biệt D13 updateInterview (cập nhật toàn bộ) và D14 rescheduleInterview
 * (chỉ đổi thời gian) để endpoint semantics rõ ràng.
 */
public interface LichPhongVanService {

    /** D9: Tạo lịch phỏng vấn (chỉ HR). */
    LichPhongVanResponse createInterview(LichPhongVanRequest request, Long taiKhoanId);

    /** D10: Danh sách lịch phỏng vấn của một đơn – kiểm tra quyền UV/HR. */
    List<LichPhongVanResponse> getInterviewsByApplication(Long donUngTuyenId, Long taiKhoanId);

    /** D11: Danh sách lịch HR đang quản lý (phân trang). */
    Page<LichPhongVanResponse> getMyInterviews(Long taiKhoanId, int page, int size);

    /** D12: Chi tiết một lịch phỏng vấn – kiểm tra quyền UV/HR. */
    LichPhongVanResponse getInterviewDetail(Long id, Long taiKhoanId);

    /** D13 PUT: Cập nhật toàn bộ thông tin lịch phỏng vấn (HR). */
    LichPhongVanResponse updateInterview(Long id, LichPhongVanRequest request, Long taiKhoanId);

    /**
     * D14 PATCH /reschedule: Chỉ đổi thời gian lịch phỏng vấn (HR).
     * Dùng RescheduleRequest (thoiGianBatDau, thoiGianKetThuc, diaDiemHoacLink)
     * thay vì LichPhongVanRequest để tránh client gửi nhầm field không liên quan.
     */
    LichPhongVanResponse rescheduleInterview(Long id, RescheduleRequest request, Long taiKhoanId);

    /** D13 DELETE: Hủy lịch phỏng vấn (chỉ HR). */
    void cancelInterview(Long id, Long taiKhoanId);
}
