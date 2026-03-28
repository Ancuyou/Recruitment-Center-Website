package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.LichPhongVanRequest;
import com.example.tuyendung.dto.request.RescheduleRequest;
import com.example.tuyendung.dto.response.LichPhongVanResponse;
import com.example.tuyendung.entity.DonUngTuyen;
import com.example.tuyendung.entity.LichPhongVan;
import com.example.tuyendung.entity.NhaTuyenDung;
import com.example.tuyendung.entity.enums.LoaiThongBao;
import com.example.tuyendung.entity.enums.TrangThaiPhongVan;
import com.example.tuyendung.exception.ApplicationNotFoundException;
import com.example.tuyendung.exception.InterviewNotFoundException;
import com.example.tuyendung.exception.InvalidInterviewTimeException;
import com.example.tuyendung.exception.UnauthorizedApplicationAccessException;
import com.example.tuyendung.repository.DonUngTuyenRepository;
import com.example.tuyendung.repository.LichPhongVanRepository;
import com.example.tuyendung.service.LichPhongVanService;
import com.example.tuyendung.service.ThongBaoService;
import com.example.tuyendung.service.util.ApplicationAccessVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Triển khai LichPhongVanService – Module D9-D14.
 *
 * Design Patterns:
 *  - Repository Pattern: giao tiếp DB qua LichPhongVanRepository.
 *  - Builder Pattern: LichPhongVanResponse.builder().
 *  - Guard Clause Pattern: validate sớm (thời gian, quyền) trước khi thực hiện.
 *
 * SOLID:
 *  - SRP: chỉ xử lý lịch phỏng vấn.
 *  - OCP: thêm trạng thái mới → mở rộng TrangThaiPhongVan enum.
 *  - DIP: phụ thuộc interface Repository/Service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LichPhongVanServiceImpl implements LichPhongVanService {

    private final LichPhongVanRepository lichPhongVanRepository;
    private final DonUngTuyenRepository donUngTuyenRepository;
    private final ThongBaoService thongBaoService;
    private final ApplicationAccessVerifier accessVerifier;

    // =========================================================================
    // D9: Tạo lịch phỏng vấn
    // =========================================================================

    @Override
    @Transactional
    public LichPhongVanResponse createInterview(LichPhongVanRequest request, Long taiKhoanId) {
        log.info("D9: Tạo lịch phỏng vấn cho đơn ID {}", request.getDonUngTuyenId());

        validateTimeRange(request.getThoiGianBatDau(), request.getThoiGianKetThuc());

        NhaTuyenDung ntd = findNhaTuyenDungByTaiKhoan(taiKhoanId);

        DonUngTuyen don = donUngTuyenRepository.findByIdWithDetails(request.getDonUngTuyenId())
                .orElseThrow(() -> new ApplicationNotFoundException(request.getDonUngTuyenId()));

        verifyNtdOwnsDon(don, ntd);

        LichPhongVan lich = new LichPhongVan();
        lich.setDonUngTuyen(don);
        lich.setNguoiPhongVan(ntd);
        lich.setTieuDeVong(request.getTieuDeVong());
        lich.setThoiGianBatDau(request.getThoiGianBatDau());
        lich.setThoiGianKetThuc(request.getThoiGianKetThuc());
        lich.setHinhThuc(request.getHinhThuc());
        lich.setDiaDiemHoacLink(request.getDiaDiemHoacLink());
        lich.setTrangThaiPhongVan(TrangThaiPhongVan.CHO_PHONG_VAN);

        LichPhongVan saved = lichPhongVanRepository.save(lich);

        // Thông báo ứng viên
        String noiDung = String.format(
                "Bạn có lịch phỏng vấn mới vòng: %s (%s). Thời gian: %s",
                lich.getTieuDeVong(), lich.getHinhThuc().name(), lich.getThoiGianBatDau());
        notifyUngVien(don, "Lịch phỏng vấn mới", noiDung, "/interviews/" + saved.getId());

        return mapToResponse(saved);
    }

    // =========================================================================
    // D10: Danh sách lịch của một đơn
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<LichPhongVanResponse> getInterviewsByApplication(Long donUngTuyenId, Long taiKhoanId) {
        // Verify đơn tồn tại
        DonUngTuyen don = donUngTuyenRepository.findByIdWithDetails(donUngTuyenId)
                .orElseThrow(() -> new ApplicationNotFoundException(donUngTuyenId));

        // Kiểm tra quyền: UV chủ đơn hoặc NTD chủ tin
        verifyInterviewAccess(don, taiKhoanId);

        return lichPhongVanRepository.findByDonUngTuyenId(donUngTuyenId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // D11: Danh sách lịch quản lý (HR)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<LichPhongVanResponse> getMyInterviews(Long taiKhoanId, int page, int size) {
        NhaTuyenDung ntd = findNhaTuyenDungByTaiKhoan(taiKhoanId);
        return lichPhongVanRepository
                .findByNguoiPhongVanId(ntd.getId(), PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // =========================================================================
    // D12: Chi tiết một lịch
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public LichPhongVanResponse getInterviewDetail(Long id, Long taiKhoanId) {
        LichPhongVan lich = findLichById(id);
        // Kiểm tra quyền: UV chủ đơn hoặc NTD chủ lịch
        verifyInterviewAccess(lich.getDonUngTuyen(), taiKhoanId);
        return mapToResponse(lich);
    }

    // =========================================================================
    // D13: Cập nhật lịch phỏng vấn
    // =========================================================================

    @Override
    @Transactional
    public LichPhongVanResponse updateInterview(Long id, LichPhongVanRequest request, Long taiKhoanId) {
        log.info("D13: Cập nhật lịch phỏng vấn {}", id);

        LichPhongVan lich = findLichById(id);
        NhaTuyenDung ntd = findNhaTuyenDungByTaiKhoan(taiKhoanId);
        verifyNtdOwnsLich(lich, ntd);

        validateTimeRange(request.getThoiGianBatDau(), request.getThoiGianKetThuc());

        lich.setTieuDeVong(request.getTieuDeVong());
        lich.setThoiGianBatDau(request.getThoiGianBatDau());
        lich.setThoiGianKetThuc(request.getThoiGianKetThuc());
        lich.setHinhThuc(request.getHinhThuc());
        lich.setDiaDiemHoacLink(request.getDiaDiemHoacLink());

        LichPhongVan saved = lichPhongVanRepository.save(lich);

        String noiDung = "Lịch phỏng vấn vòng " + lich.getTieuDeVong() + " đã được cập nhật.";
        notifyUngVien(lich.getDonUngTuyen(), "Cập nhật lịch phỏng vấn", noiDung, "/interviews/" + saved.getId());

        return mapToResponse(saved);
    }

    // =========================================================================
    // D14: Đổi lịch phỏng vấn (Reschedule – khác Update ở chỗ chỉ đổi thời gian)
    // =========================================================================

    @Override
    @Transactional
    public LichPhongVanResponse rescheduleInterview(Long id, RescheduleRequest request, Long taiKhoanId) {
        log.info("D14: Dời lịch phỏng vấn {}", id);

        LichPhongVan lich = findLichById(id);
        NhaTuyenDung ntd = findNhaTuyenDungByTaiKhoan(taiKhoanId);
        verifyNtdOwnsLich(lich, ntd);

        validateTimeRange(request.getThoiGianBatDau(), request.getThoiGianKetThuc());

        // Reschedule: chỉ cập nhật thời gian và địa điểm (không thay đổi tiêu đề vòng/hình thức)
        lich.setThoiGianBatDau(request.getThoiGianBatDau());
        lich.setThoiGianKetThuc(request.getThoiGianKetThuc());
        if (request.getDiaDiemHoacLink() != null) {
            lich.setDiaDiemHoacLink(request.getDiaDiemHoacLink());
        }

        LichPhongVan saved = lichPhongVanRepository.save(lich);

        String noiDung = String.format(
                "Lịch phỏng vấn vòng '%s' đã được dời sang: %s.",
                lich.getTieuDeVong(), lich.getThoiGianBatDau());
        notifyUngVien(lich.getDonUngTuyen(), "Dời lịch phỏng vấn", noiDung, "/interviews/" + saved.getId());

        return mapToResponse(saved);
    }

    // =========================================================================
    // D13 (DELETE): Hủy lịch phỏng vấn
    // =========================================================================

    @Override
    @Transactional
    public void cancelInterview(Long id, Long taiKhoanId) {
        log.info("D13(DELETE): Hủy lịch phỏng vấn {}", id);

        LichPhongVan lich = findLichById(id);
        NhaTuyenDung ntd = findNhaTuyenDungByTaiKhoan(taiKhoanId);
        verifyNtdOwnsLich(lich, ntd);

        if (lich.getTrangThaiPhongVan() == TrangThaiPhongVan.HUY) {
            throw new InvalidInterviewTimeException("Lịch này đã được hủy trước đó");
        }

        lich.setTrangThaiPhongVan(TrangThaiPhongVan.HUY);
        lichPhongVanRepository.save(lich);

        String noiDung = "Lịch phỏng vấn vòng " + lich.getTieuDeVong() + " đã bị hủy.";
        notifyUngVien(lich.getDonUngTuyen(), "Hủy phỏng vấn", noiDung, null);
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    private LichPhongVan findLichById(Long id) {
        return lichPhongVanRepository.findById(id)
                .orElseThrow(() -> new InterviewNotFoundException(id));
    }

    private NhaTuyenDung findNhaTuyenDungByTaiKhoan(Long taiKhoanId) {
        return accessVerifier.findNhaTuyenDungByTaiKhoan(taiKhoanId);
    }

    /** Validate khoảng thời gian phỏng vấn hợp lệ. */
    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new InvalidInterviewTimeException();
        }
    }

    /** Kiểm tra NTD là chủ sở hữu của tin liên quan đến đơn. */
    private void verifyNtdOwnsDon(DonUngTuyen don, NhaTuyenDung ntd) {
        if (!don.getTinTuyenDung().getNhaTuyenDung().getId().equals(ntd.getId())) {
            throw new UnauthorizedApplicationAccessException(don.getId());
        }
    }

    /** Kiểm tra NTD là người phỏng vấn của lịch. */
    private void verifyNtdOwnsLich(LichPhongVan lich, NhaTuyenDung ntd) {
        if (!lich.getDonUngTuyen().getTinTuyenDung().getNhaTuyenDung().getId().equals(ntd.getId())) {
            throw new UnauthorizedApplicationAccessException();
        }
    }

    /**
     * Kiểm tra quyền xem lịch phỏng vấn:
     * - UNG_VIEN: chỉ xem lịch của đơn do mình nộp.
     * - NHA_TUYEN_DUNG: chỉ xem lịch thuộc tin của mình.
     */
    private void verifyInterviewAccess(DonUngTuyen don, Long taiKhoanId) {
        accessVerifier.verifyApplicationAccess(don, taiKhoanId);
    }

    /** Gửi thông báo đến ứng viên của đơn. */
    private void notifyUngVien(DonUngTuyen don, String tieuDe, String noiDung, String lienKet) {
        Long taiKhoanUv = don.getHoSoCv().getUngVien().getTaiKhoan().getId();
        thongBaoService.createNotification(taiKhoanUv, tieuDe, noiDung, LoaiThongBao.PHONG_VAN_TAO_MOI, lienKet);
    }

    /** Map entity → DTO (Builder Pattern). */
    private LichPhongVanResponse mapToResponse(LichPhongVan l) {
        return LichPhongVanResponse.builder()
                .id(l.getId())
                .donUngTuyenId(l.getDonUngTuyen().getId())
                .tieuDeTin(l.getDonUngTuyen().getTinTuyenDung().getTieuDe())
                .tenUngVien(l.getDonUngTuyen().getHoSoCv().getUngVien().getHoTen())
                .nguoiPhongVanId(l.getNguoiPhongVan().getId())
                .tenNguoiPhongVan(l.getNguoiPhongVan().getHoTen())
                .tieuDeVong(l.getTieuDeVong())
                .thoiGianBatDau(l.getThoiGianBatDau())
                .thoiGianKetThuc(l.getThoiGianKetThuc())
                .hinhThuc(l.getHinhThuc())
                .diaDiemHoacLink(l.getDiaDiemHoacLink())
                .trangThai(l.getTrangThaiPhongVan())
                .ngayTao(l.getNgayTao())
                .build();
    }
}
