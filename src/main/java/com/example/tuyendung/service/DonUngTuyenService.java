package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.DonUngTuyenRequest;
import com.example.tuyendung.dto.request.TrangThaiDonRequest;
import com.example.tuyendung.dto.response.DonUngTuyenResponse;
import org.springframework.data.domain.Page;

/**
 * Service interface nghiệp vụ đơn ứng tuyển – Module D1-D8.
 *
 * SOLID:
 * - SRP: chỉ khai báo các method liên quan đến DonUngTuyen.
 * - OCP: thêm logic mới → implement class, không sửa interface này.
 * - ISP: không khai báo method không liên quan module D.
 * - DIP: controller/test chỉ phụ thuộc interface này.
 */
public interface DonUngTuyenService {

    /** D1: Nộp đơn ứng tuyển – trả về đơn vừa tạo. */
    DonUngTuyenResponse submitApplication(DonUngTuyenRequest request, Long taiKhoanId);

    /** D2: Danh sách đơn đã nộp của ứng viên (phân trang). */
    Page<DonUngTuyenResponse> getCandidateApplications(Long taiKhoanId, int page, int size);

    /** D3: Danh sách đơn nhận được cho một tin tuyển dụng (phân trang, kiểm tra quyền HR). */
    Page<DonUngTuyenResponse> getRecruiterApplications(Long tinTuyenDungId, Long taiKhoanId, int page, int size);

    /** D4: Chi tiết một đơn – kiểm tra quyền (UV chủ đơn hoặc HR chủ tin). */
    DonUngTuyenResponse getApplicationDetail(Long id, Long taiKhoanId);

    /** D5: Cập nhật trạng thái đơn (chỉ HR) – ghi lịch sử và bắn thông báo. */
    DonUngTuyenResponse updateStatus(Long id, TrangThaiDonRequest request, Long taiKhoanId);

    /**
     * D6: Từ chối đơn (chỉ HR).
     * Tách riêng với D5 để endpoint semantics rõ ràng.
     *
     * @param id          ID đơn cần từ chối
     * @param ghiChu      Ghi chú lý do từ chối (có thể null)
     * @param taiKhoanId  ID tài khoản HR thực hiện
     */
    DonUngTuyenResponse rejectApplication(Long id, String ghiChu, Long taiKhoanId);

    /** D7: Lấy URL ảnh snapshot CV đã lưu khi nộp (kiểm tra quyền). */
    String getCvSnapshotUrl(Long id, Long taiKhoanId);
}
