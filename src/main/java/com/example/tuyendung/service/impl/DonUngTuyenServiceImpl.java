package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.DonUngTuyenRequest;
import com.example.tuyendung.dto.request.TrangThaiDonRequest;
import com.example.tuyendung.dto.response.DonUngTuyenResponse;
import com.example.tuyendung.entity.DonUngTuyen;
import com.example.tuyendung.entity.HoSoCv;
import com.example.tuyendung.entity.LichSuTrangThai;
import com.example.tuyendung.entity.NhaTuyenDung;
import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.entity.TinTuyenDung;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.entity.enums.LoaiThongBao;
import com.example.tuyendung.entity.enums.TrangThaiDon;
import com.example.tuyendung.exception.ApplicationNotFoundException;
import com.example.tuyendung.exception.DuplicateApplicationException;
import com.example.tuyendung.exception.ResourceNotFoundException;
import com.example.tuyendung.exception.UnauthorizedApplicationAccessException;
import com.example.tuyendung.exception.ValidationException;
import com.example.tuyendung.repository.DonUngTuyenRepository;
import com.example.tuyendung.repository.HoSoCvRepository;
import com.example.tuyendung.repository.LichSuTrangThaiRepository;
import com.example.tuyendung.repository.TaiKhoanRepository;
import com.example.tuyendung.repository.TinTuyenDungRepository;
import com.example.tuyendung.service.DonUngTuyenService;
import com.example.tuyendung.service.ThongBaoService;
import com.example.tuyendung.service.util.ApplicationAccessVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai DonUngTuyenService – Module D1-D8.
 *
 * Design Patterns áp dụng:
 *  - Repository Pattern: giao tiếp DB qua các Repository interface.
 *  - Builder Pattern: DonUngTuyenResponse.builder() để tạo DTO không lỗi.
 *  - Template Method (ngầm): verifyApplicationAccess / verifyRecruiterOwnership
 *    được dùng chung cho nhiều method, đảm bảo DRY.
 *
 * SOLID:
 *  - SRP: chỉ xử lý nghiệp vụ đơn ứng tuyển (không xử lý thông báo trực tiếp).
 *  - OCP: thêm trạng thái mới → chỉ cần thêm constant TrangThaiDon, không sửa class này.
 *  - LSP: implement đúng contract DonUngTuyenService.
 *  - DIP: phụ thuộc các interface Repository/Service, không phụ thuộc implement cụ thể.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DonUngTuyenServiceImpl implements DonUngTuyenService {

    private final DonUngTuyenRepository donUngTuyenRepository;
    private final HoSoCvRepository hoSoCvRepository;
    private final TinTuyenDungRepository tinTuyenDungRepository;
    private final LichSuTrangThaiRepository lichSuTrangThaiRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final ThongBaoService thongBaoService;
    private final ApplicationAccessVerifier accessVerifier;

    // =========================================================================
    // D1: Nộp đơn
    // =========================================================================

    @Override
    @Transactional
    public DonUngTuyenResponse submitApplication(DonUngTuyenRequest request, Long taiKhoanId) {
        log.info("D1: Ứng viên {} nộp đơn vào Tin {}", taiKhoanId, request.getTinTuyenDungId());

        UngVien uv = findUngVienByTaiKhoan(taiKhoanId);

        // Kiểm tra nộp trùng
        if (donUngTuyenRepository.existsByTinTuyenDungIdAndTaiKhoanId(
                request.getTinTuyenDungId(), taiKhoanId)) {
            throw new DuplicateApplicationException(request.getTinTuyenDungId(), taiKhoanId);
        }

        TinTuyenDung tin = tinTuyenDungRepository.findByIdAndNotDeleted(request.getTinTuyenDungId())
                .orElseThrow(() -> new ResourceNotFoundException("TinTuyenDung", request.getTinTuyenDungId()));

        HoSoCv cv = hoSoCvRepository.findByIdAndNotDeleted(request.getHoSoCvId())
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", request.getHoSoCvId()));

        // Ứng viên chỉ được dùng CV của chính mình
        if (!cv.getUngVien().getId().equals(uv.getId())) {
            throw new UnauthorizedApplicationAccessException();
        }

        DonUngTuyen don = new DonUngTuyen();
        don.setTinTuyenDung(tin);
        don.setHoSoCv(cv);
        don.setThuNgo(request.getThuNgo());
        don.setBanSaoCvUrl(cv.getFileCvUrl()); // Snapshot tại thời điểm nộp
        don.setTrangThaiHienTai(TrangThaiDon.MOI.getValue());

        DonUngTuyen saved = donUngTuyenRepository.save(don);

        recordStatusHistory(saved, null, TrangThaiDon.MOI.getValue(), "Ứng viên nộp đơn", taiKhoanId);

        return mapToResponse(saved);
    }

    // =========================================================================
    // D2: Danh sách đơn – Candidate
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<DonUngTuyenResponse> getCandidateApplications(Long taiKhoanId, int page, int size) {
        UngVien uv = findUngVienByTaiKhoan(taiKhoanId);
        return donUngTuyenRepository
                .findByUngVienId(uv.getId(), PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // =========================================================================
    // D3: Danh sách đơn – Recruiter
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<DonUngTuyenResponse> getRecruiterApplications(Long tinTuyenDungId, Long taiKhoanId, int page, int size) {
        NhaTuyenDung ntd = findNhaTuyenDungByTaiKhoan(taiKhoanId);

        TinTuyenDung tin = tinTuyenDungRepository.findById(tinTuyenDungId)
                .orElseThrow(() -> new ResourceNotFoundException("TinTuyenDung", tinTuyenDungId));

        // Kiểm tra tin thuộc NTD đang request
        if (!tin.getNhaTuyenDung().getId().equals(ntd.getId())) {
            throw new UnauthorizedApplicationAccessException();
        }

        return donUngTuyenRepository
                .findByTinTuyenDungIdAndNhaTuyenDungId(tinTuyenDungId, ntd.getId(), PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // =========================================================================
    // D4: Chi tiết đơn
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public DonUngTuyenResponse getApplicationDetail(Long id, Long taiKhoanId) {
        DonUngTuyen don = donUngTuyenRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));

        verifyApplicationAccess(don, taiKhoanId);
        return mapToResponse(don);
    }

    // =========================================================================
    // D5: Cập nhật trạng thái
    // =========================================================================

    @Override
    @Transactional
    public DonUngTuyenResponse updateStatus(Long id, TrangThaiDonRequest request, Long taiKhoanId) {
        log.info("D5: Cập nhật trạng thái đơn {} → {}", id, request.getTrangThaiMoi());

        DonUngTuyen don = donUngTuyenRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));

        verifyRecruiterOwnership(don, taiKhoanId);

        TrangThaiDon newStatus = TrangThaiDon.fromValue(request.getTrangThaiMoi());
        int oldStatus = don.getTrangThaiHienTai();

        if (oldStatus == newStatus.getValue()) {
            throw new ValidationException("status", "Trạng thái này đã được thiết lập");
        }

        don.setTrangThaiHienTai(newStatus.getValue());
        DonUngTuyen saved = donUngTuyenRepository.save(don);

        recordStatusHistory(saved, oldStatus, newStatus.getValue(), request.getGhiChu(), taiKhoanId);
        sendStatusChangeNotification(saved, newStatus, taiKhoanId);

        return mapToResponse(saved);
    }

    // =========================================================================
    // D6: Từ chối đơn
    // =========================================================================

    @Override
    @Transactional
    public DonUngTuyenResponse rejectApplication(Long id, String ghiChu, Long taiKhoanId) {
        log.info("D6: Từ chối đơn {}", id);

        // Tái sử dụng D5 với trạng thái TU_CHOI cố định
        TrangThaiDonRequest request = new TrangThaiDonRequest();
        request.setTrangThaiMoi(TrangThaiDon.TU_CHOI.getValue());
        request.setGhiChu(ghiChu);

        return updateStatus(id, request, taiKhoanId);
    }

    // =========================================================================
    // D7: CV Snapshot
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public String getCvSnapshotUrl(Long id, Long taiKhoanId) {
        DonUngTuyen don = donUngTuyenRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));

        verifyApplicationAccess(don, taiKhoanId);
        return don.getBanSaoCvUrl();
    }

    // =========================================================================
    // D8: Lịch sử trạng thái (moved to LichSuTrangThaiService)
    // =========================================================================

    // =========================================================================
    // Private Helper Methods
    // =========================================================================

    /** Tìm UngVien theo taiKhoanId – ném BusinessException nếu không tồn tại. */
    private UngVien findUngVienByTaiKhoan(Long taiKhoanId) {
        return accessVerifier.findUngVienByTaiKhoan(taiKhoanId);
    }

    /** Tìm NhaTuyenDung theo taiKhoanId – ném BusinessException nếu không tồn tại. */
    private NhaTuyenDung findNhaTuyenDungByTaiKhoan(Long taiKhoanId) {
        return accessVerifier.findNhaTuyenDungByTaiKhoan(taiKhoanId);
    }

    /**
     * Kiểm tra NTD có quyền thao tác trên đơn hay không
     * (tin tuyển dụng phải thuộc NTD đang thực hiện).
     */
    private void verifyRecruiterOwnership(DonUngTuyen don, Long taiKhoanId) {
        accessVerifier.verifyRecruiterOwnsApplication(don, taiKhoanId);
    }

    /**
     * Kiểm tra quyền xem đơn:
     * - UNG_VIEN: chỉ xem đơn do mình nộp.
     * - NHA_TUYEN_DUNG: chỉ xem đơn thuộc tin của mình.
     * - ADMIN: xem được tất cả.
     */
    private void verifyApplicationAccess(DonUngTuyen don, Long taiKhoanId) {
        accessVerifier.verifyApplicationAccess(don, taiKhoanId);
    }

    /** Ghi lại một bước thay đổi trạng thái vào bảng lich_su_trang_thai. */
    private void recordStatusHistory(DonUngTuyen don, Integer oldStatus, Integer newStatus,
                                     String note, Long taiKhoanId) {
        TaiKhoan tk = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new ResourceNotFoundException("TaiKhoan", taiKhoanId));

        LichSuTrangThai ls = new LichSuTrangThai();
        ls.setDonUngTuyen(don);
        ls.setNguoiThucHien(tk);
        ls.setTrangThaiCu(oldStatus);
        ls.setTrangThaiMoi(newStatus);
        ls.setGhiChu(note);
        // thoi_gian_chuyen: được DB tự set (insertable=false, updatable=false)

        lichSuTrangThaiRepository.save(ls);
    }

    /** Gửi thông báo đến ứng viên khi trạng thái đơn thay đổi. */
    private void sendStatusChangeNotification(DonUngTuyen don, TrangThaiDon newStatus, Long taiKhoanId) {
        Long taiKhoanUv = don.getHoSoCv().getUngVien().getTaiKhoan().getId();
        String tieuDeJob = don.getTinTuyenDung().getTieuDe();
        String noiDung = String.format(
                "Đơn ứng tuyển vị trí '%s' của bạn đã được cập nhật thành: %s.",
                tieuDeJob, newStatus.getLabel());

        thongBaoService.createNotification(
                taiKhoanUv,
                "Cập nhật đơn ứng tuyển",
                noiDung,
                LoaiThongBao.UNG_TUYEN_VE_PHONG_VAN,
                "/applications/" + don.getId()
        );
    }

    /** Map entity → DTO response (Builder Pattern). */
    private DonUngTuyenResponse mapToResponse(DonUngTuyen d) {
        TrangThaiDon status = TrangThaiDon.fromValue(d.getTrangThaiHienTai());
        return DonUngTuyenResponse.builder()
                .id(d.getId())
                .tinTuyenDungId(d.getTinTuyenDung().getId())
                .tieuDeTin(d.getTinTuyenDung().getTieuDe())
                .tenCongTy(d.getTinTuyenDung().getCongTy().getTenCongTy())
                .logoCongTy(d.getTinTuyenDung().getCongTy().getLogoUrl())
                .ungVienId(d.getHoSoCv().getUngVien().getId())
                .tenUngVien(d.getHoSoCv().getUngVien().getHoTen())
                .emailUngVien(d.getHoSoCv().getUngVien().getTaiKhoan().getEmail())
                .hoSoCvId(d.getHoSoCv().getId())
                .cvUrl(d.getBanSaoCvUrl())
                .thuNgo(d.getThuNgo())
                .trangThai(d.getTrangThaiHienTai())
                .trangThaiLabel(status.getLabel())
                .ngayNop(d.getNgayNop())
                .build();
    }
}
