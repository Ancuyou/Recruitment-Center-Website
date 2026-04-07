package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.TinTuyenDungRequest;
import com.example.tuyendung.dto.response.JobStatisticsResponse;
import com.example.tuyendung.dto.response.TinTuyenDungResponse;
import com.example.tuyendung.entity.NhaTuyenDung;
import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.entity.TinTuyenDung;
import com.example.tuyendung.entity.enums.CapBacYeuCau;
import com.example.tuyendung.entity.enums.HinhThucLamViec;
import com.example.tuyendung.entity.enums.KhuVucEnum;
import com.example.tuyendung.entity.enums.TrangThaiDon;
import com.example.tuyendung.entity.enums.TrangThaiTin;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.NhaTuyenDungRepository;
import com.example.tuyendung.repository.TaiKhoanRepository;
import com.example.tuyendung.repository.TinTuyenDungRepository;
import com.example.tuyendung.service.TinTuyenDungService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service impl cho tin tuyển dụng (B13-B22)
 *
 * SOLID:
 * - SRP: Chỉ lo business logic tin tuyển dụng
 * - DIP: Inject qua interfaces
 *
 * Khu vực dùng enum, quản lý trực tiếp qua @ElementCollection
 * → Không cần KhuVucRepository hay CtKvTinRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TinTuyenDungServiceImpl implements TinTuyenDungService {

    private final TinTuyenDungRepository tinTuyenDungRepository;
    private final NhaTuyenDungRepository nhaTuyenDungRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    // ── B13 ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TinTuyenDungResponse createTin(TinTuyenDungRequest request, Long taiKhoanId) {
        log.info("B13: Tạo tin tuyển dụng bởi tài khoản {}", taiKhoanId);
        NhaTuyenDung ntd = findNtdOrThrow(taiKhoanId);
        validateSalary(request.getMucLuongMin(), request.getMucLuongMax());

        TinTuyenDung tin = new TinTuyenDung();
        tin.setNhaTuyenDung(ntd);
        tin.setCongTy(ntd.getCongTy());
        applyRequest(request, tin);
        tin.setTrangThai(TrangThaiTin.MO);

        TinTuyenDung saved = tinTuyenDungRepository.save(tin);
        log.info("Tạo tin thành công, ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    // ── B14 ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<TinTuyenDungResponse> getActiveTins(int page, int size) {
        return tinTuyenDungRepository.findActiveJobs(PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // ── B15 ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public TinTuyenDungResponse getTinById(Long id) {
        TinTuyenDung tin = tinTuyenDungRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.JOB_NOT_FOUND,
                        "Không tìm thấy tin tuyển dụng ID: " + id));
        return mapToResponse(tin);
    }

    // ── B16 ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TinTuyenDungResponse updateTin(Long id, TinTuyenDungRequest request, Long taiKhoanId) {
        log.info("B16: Cập nhật tin ID: {}", id);
        NhaTuyenDung ntd = findNtdOrThrow(taiKhoanId);
        TinTuyenDung tin = findActiveTinOrThrow(id);
        verifyTinOwnership(ntd, tin);
        validateSalary(request.getMucLuongMin(), request.getMucLuongMax());
        applyRequest(request, tin);
        return mapToResponse(tinTuyenDungRepository.save(tin));
    }

    // ── B17 ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void closeTin(Long id, Long taiKhoanId) {
        log.info("B17: Đóng tin ID: {}", id);
        TinTuyenDung tin = findActiveTinOrThrow(id);
        verifyTinAccess(tin, taiKhoanId);
        tin.setTrangThai(TrangThaiTin.DONG);
        tinTuyenDungRepository.save(tin);
    }

    // ── B18 ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TinTuyenDungResponse> getMyTins(Long taiKhoanId) {
        NhaTuyenDung ntd = findNtdOrThrow(taiKhoanId);
        return tinTuyenDungRepository.findByNhaTuyenDungIdOrderByNgayTaoDesc(ntd.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── B19 ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<TinTuyenDungResponse> searchTins(
            String keyword, CapBacYeuCau capBac, HinhThucLamViec hinhThuc,
            BigDecimal mucLuongMin, int page, int size) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return tinTuyenDungRepository
                .searchJobs(kw, capBac, hinhThuc, mucLuongMin, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // ── B20: Cập nhật khu vực (set toàn bộ) ──────────────────────────────────

    @Override
    @Transactional
    public TinTuyenDungResponse updateKhuVucs(Long tinId, Set<KhuVucEnum> khuVucs, Long taiKhoanId) {
        log.info("B20: Cập nhật khu vực cho tin {}", tinId);
        NhaTuyenDung ntd = findNtdOrThrow(taiKhoanId);
        TinTuyenDung tin = findActiveTinOrThrow(tinId);
        verifyTinOwnership(ntd, tin);
        tin.getKhuVucs().clear();
        tin.getKhuVucs().addAll(khuVucs);
        return mapToResponse(tinTuyenDungRepository.save(tin));
    }

    // ── B21 ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Set<KhuVucEnum> getKhuVucsOfTin(Long tinId) {
        // Copy ra HashSet mới: tránh LazyCollectionException khi Set bị serialize ngoài transaction
        return new java.util.HashSet<>(findActiveTinOrThrow(tinId).getKhuVucs());
    }

    // ── B22 ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public JobStatisticsResponse getJobStatistics(Long tinId, Long taiKhoanId) {
        TinTuyenDung tin = tinTuyenDungRepository.findByIdIncludingDeleted(tinId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.JOB_NOT_FOUND,
                        "Không tìm thấy tin tuyển dụng ID: " + tinId));
        verifyTinAccess(tin, taiKhoanId);
        return JobStatisticsResponse.builder()
                .tinId(tinId)
                .tieuDe(tin.getTieuDe())
                .tongSoDon(tinTuyenDungRepository.countTotalApplications(tinId))
                .soMoi(tinTuyenDungRepository.countApplicationsByStatus(tinId, TrangThaiDon.MOI))
                .soReview(tinTuyenDungRepository.countApplicationsByStatus(tinId, TrangThaiDon.REVIEW))
                .soPhongVan(tinTuyenDungRepository.countApplicationsByStatus(tinId, TrangThaiDon.PHONG_VAN))
                .soOffer(tinTuyenDungRepository.countApplicationsByStatus(tinId, TrangThaiDon.OFFER))
                .soTuChoi(tinTuyenDungRepository.countApplicationsByStatus(tinId, TrangThaiDon.TU_CHOI))
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private NhaTuyenDung findNtdOrThrow(Long taiKhoanId) {
        return nhaTuyenDungRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.RECRUITER_NOT_FOUND));
    }

    private TaiKhoan findTaiKhoanOrThrow(Long taiKhoanId) {
        return taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private TinTuyenDung findActiveTinOrThrow(Long id) {
        return tinTuyenDungRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.JOB_NOT_FOUND,
                        "Tin tuyển dụng không tồn tại hoặc đã bị đóng: " + id));
    }

    /**
     * Guard clause: kiểm tra quyền sở hữu tin
     * SRP: Tách biệt authorization logic
     * Nhận pre-fetched NhaTuyenDung để tránh double DB lookup
     */
    private void verifyTinOwnership(NhaTuyenDung ntd, TinTuyenDung tin) {
        if (!tin.getNhaTuyenDung().getId().equals(ntd.getId())) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Bạn không có quyền thao tác với tin này");
        }
    }

    /**
     * Admin được phép thao tác mọi tin; recruiter chỉ thao tác trên tin của mình.
     */
    private void verifyTinAccess(TinTuyenDung tin, Long taiKhoanId) {
        TaiKhoan taiKhoan = findTaiKhoanOrThrow(taiKhoanId);
        if (taiKhoan.getVaiTro() == VaiTroTaiKhoan.ADMIN) {
            return;
        }
        if (taiKhoan.getVaiTro() == VaiTroTaiKhoan.NHA_TUYEN_DUNG) {
            NhaTuyenDung ntd = findNtdOrThrow(taiKhoanId);
            verifyTinOwnership(ntd, tin);
            return;
        }
        throw new BaseBusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    private void validateSalary(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BaseBusinessException(ErrorCode.SALARY_RANGE_INVALID);
        }
    }

    private void applyRequest(TinTuyenDungRequest request, TinTuyenDung tin) {
        tin.setTieuDe(request.getTieuDe().trim());
        tin.setMoTaCongViec(request.getMoTaCongViec());
        tin.setYeuCauUngVien(request.getYeuCauUngVien());
        tin.setMucLuongMin(request.getMucLuongMin());
        tin.setMucLuongMax(request.getMucLuongMax());
        tin.setDiaDiem(request.getDiaDiem());
        tin.setCapBacYeuCau(request.getCapBacYeuCau());
        tin.setHinhThucLamViec(request.getHinhThucLamViec());
        tin.setHanNop(request.getHanNop());
    }

    private TinTuyenDungResponse mapToResponse(TinTuyenDung tin) {
        TrangThaiTin trangThai = tin.getTrangThai();
        return TinTuyenDungResponse.builder()
                .id(tin.getId())
                .nhaTuyenDungId(tin.getNhaTuyenDung().getId())
                .tenNhaTuyenDung(tin.getNhaTuyenDung().getHoTen())
                .congTyId(tin.getCongTy().getId())
                .tenCongTy(tin.getCongTy().getTenCongTy())
                .logoUrl(tin.getCongTy().getLogoUrl())
                .tieuDe(tin.getTieuDe())
                .moTaCongViec(tin.getMoTaCongViec())
                .yeuCauUngVien(tin.getYeuCauUngVien())
                .mucLuongMin(tin.getMucLuongMin())
                .mucLuongMax(tin.getMucLuongMax())
                .diaDiem(tin.getDiaDiem())
                .capBacYeuCau(tin.getCapBacYeuCau())
                .hinhThucLamViec(tin.getHinhThucLamViec())
                .hanNop(tin.getHanNop())
                .trangThai(tin.getTrangThai().getValue())
                .trangThaiLabel(trangThai.getLabel())
                .ngayTao(tin.getNgayTao())
                .ngayCapNhat(tin.getNgayCapNhat())
                .khuVucs(tin.getKhuVucs())
                .soLuongDon(tinTuyenDungRepository.countTotalApplications(tin.getId()))
                .build();
    }
}
