package com.example.tuyendung.service;

import com.example.tuyendung.dto.response.LichSuTrangThaiResponse;
import org.springframework.data.domain.Page;

/**
 * Service interface cho lịch sử trạng thái đơn ứng tuyển – Module D8.
 *
 * SOLID:
 *  - SRP: Chỉ khai báo method liên quan đến status history
 *  - ISP: Không khai báo method không cần thiết
 *  - DIP: Controller phụ thuộc interface này, không phụ thuộc implementation
 */
public interface LichSuTrangThaiService {

    /**
     * D8: Lấy lịch sử trạng thái của một đơn ứng tuyển (phân trang).
     *
     * @param id                ID đơn ứng tuyển
     * @param taiKhoanId        ID tài khoản người request
     * @param page              Trang (0-based)
     * @param size              Kích thước trang (1-100)
     * @return                  Page<LichSuTrangThaiResponse>
     * @throws ApplicationNotFoundException nếu đơn không tồn tại
     * @throws UnauthorizedApplicationAccessException nếu người dùng không có quyền
     */
    Page<LichSuTrangThaiResponse> getStatusHistory(Long id, Long taiKhoanId, int page, int size);
}
