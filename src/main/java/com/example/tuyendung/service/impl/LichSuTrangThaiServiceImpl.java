package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.response.LichSuTrangThaiResponse;
import com.example.tuyendung.entity.DonUngTuyen;
import com.example.tuyendung.entity.LichSuTrangThai;
import com.example.tuyendung.entity.enums.TrangThaiDon;


import com.example.tuyendung.repository.DonUngTuyenRepository;
import com.example.tuyendung.repository.LichSuTrangThaiRepository;
import com.example.tuyendung.service.LichSuTrangThaiService;
import com.example.tuyendung.entity.NhaTuyenDung;
import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.NhaTuyenDungRepository;
import com.example.tuyendung.repository.TaiKhoanRepository;
import com.example.tuyendung.repository.UngVienRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai LichSuTrangThaiService – Module D8.
 *
 * Design Patterns:
 *  - Repository Pattern: giao tiếp DB qua repository interface
 *  - Guard Clause Pattern: validate quyền trước khi lấy dữ liệu
 *
 * SOLID:
 *  - SRP: Chỉ xử lý logic lịch sử trạng thái
 *  - OCP: Thêm trạng thái mới → mở rộng enum, không sửa class này
 *  - LSP: Implement đúng contract LichSuTrangThaiService
 *  - DIP: Phụ thuộc interface Repository/Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LichSuTrangThaiServiceImpl implements LichSuTrangThaiService {

    private final LichSuTrangThaiRepository lichSuTrangThaiRepository;
    private final DonUngTuyenRepository donUngTuyenRepository;
    private final UngVienRepository ungVienRepository;
    private final NhaTuyenDungRepository nhaTuyenDungRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    // =========================================================================
    // D8: Lịch sử trạng thái
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<LichSuTrangThaiResponse> getStatusHistory(Long id, Long taiKhoanId, int page, int size) {
        log.info("D8: Lấy lịch sử trạng thái đơn {} từ tài khoản {}", id, taiKhoanId);

        // Lấy đơn ứng tuyển (kèm theo tin + công ty)
        DonUngTuyen don = donUngTuyenRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        // Kiểm tra quyền: Chỉ ứng viên chủ đơn hoặc HR chủ tin mới được xem
        verifyApplicationAccess(don, taiKhoanId);

        // Lấy lịch sử theo Page (tối ưu DB-level pagination, không load all)
        return lichSuTrangThaiRepository
                .findByDonUngTuyenIdPageable(id, org.springframework.data.domain.PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    private void verifyApplicationAccess(DonUngTuyen don, Long taiKhoanId) {
        TaiKhoan tk = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.USER_NOT_FOUND));

        if (tk.getVaiTro() == VaiTroTaiKhoan.UNG_VIEN) {
            UngVien uv = ungVienRepository.findByTaiKhoanId(taiKhoanId).orElse(null);
            if (uv == null || !don.getHoSoCv().getUngVien().getId().equals(uv.getId())) {
                throw new BaseBusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
            }
        } else if (tk.getVaiTro() == VaiTroTaiKhoan.NHA_TUYEN_DUNG) {
            NhaTuyenDung ntd = nhaTuyenDungRepository.findByTaiKhoanId(taiKhoanId)
                    .orElseThrow(() -> new BaseBusinessException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy nhà tuyển dụng"));
            if (!don.getTinTuyenDung().getNhaTuyenDung().getId().equals(ntd.getId())) {
                throw new BaseBusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
            }
        }
        // ADMIN: cho phép xem toàn bộ
    }

    /** Mapper: LichSuTrangThai → LichSuTrangThaiResponse */
    private LichSuTrangThaiResponse mapToResponse(LichSuTrangThai lstt) {
        return LichSuTrangThaiResponse.builder()
                .id(lstt.getId())
                .donUngTuyenId(lstt.getDonUngTuyen().getId())
                .nguoiThucHien(lstt.getNguoiThucHien() != null ? lstt.getNguoiThucHien().getEmail() : null)
                .vaiTro(lstt.getNguoiThucHien() != null ? lstt.getNguoiThucHien().getVaiTro().name() : null)
                .trangThaiCu(lstt.getTrangThaiCu())
                .trangThaiCuLabel(lstt.getTrangThaiCu() != null
                        ? TrangThaiDon.fromValue(lstt.getTrangThaiCu()).getLabel() : null)
                .trangThaiMoi(lstt.getTrangThaiMoi())
                .trangThaiMoiLabel(TrangThaiDon.fromValue(lstt.getTrangThaiMoi()).getLabel())
                .ghiChu(lstt.getGhiChu())
                .thoiGianChuyen(lstt.getThoiGianChuyen())
                .build();
    }
}
