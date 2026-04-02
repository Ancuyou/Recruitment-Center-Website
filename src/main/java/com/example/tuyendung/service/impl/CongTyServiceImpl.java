package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.CongTyRequest;
import com.example.tuyendung.dto.response.CongTyResponse;
import com.example.tuyendung.entity.CongTy;
import com.example.tuyendung.entity.NhaTuyenDung;
import com.example.tuyendung.entity.enums.NganhNgheEnum;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.CongTyRepository;
import com.example.tuyendung.repository.NhaTuyenDungRepository;
import com.example.tuyendung.service.CongTyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service impl cho công ty (B1-B8)
 *
 * SOLID:
 * - SRP: Chỉ xử lý nghiệp vụ công ty
 * - DIP: Inject qua interfaces
 *
 * Ngành nghề dùng enum, quản lý trực tiếp qua @ElementCollection
 * → Không cần NganhNgheRepository hay CtCtyNganhRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CongTyServiceImpl implements CongTyService {

    private final CongTyRepository congTyRepository;
    private final NhaTuyenDungRepository nhaTuyenDungRepository;

    // ── B1 ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CongTyResponse createCongTy(CongTyRequest request, Long taiKhoanId) {
        log.info("B1: Tạo công ty mới bởi tài khoản {}", taiKhoanId);

        if (request.getMaSoThue() != null && congTyRepository.existsByMaSoThue(request.getMaSoThue())) {
            throw new BaseBusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Mã số thuế '" + request.getMaSoThue() + "' đã được đăng ký");
        }

        CongTy congTy = new CongTy();
        applyRequest(request, congTy);
        CongTy saved = congTyRepository.save(congTy);
        log.info("Tạo công ty thành công, ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    // ── B2 ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CongTyResponse> getAllCongTy() {
        log.info("B2: Lấy danh sách công ty");
        return congTyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── B3 ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CongTyResponse getCongTyById(Long id) {
        log.info("B3: Chi tiết công ty ID: {}", id);
        return mapToResponse(findOrThrow(id));
    }

    // ── B4 ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CongTyResponse updateCongTy(Long id, CongTyRequest request, Long taiKhoanId) {
        log.info("B4: Cập nhật công ty ID: {}", id);
        CongTy congTy = findOrThrow(id);
        verifyOwnership(taiKhoanId, id);

        if (request.getMaSoThue() != null
                && !request.getMaSoThue().equals(congTy.getMaSoThue())
                && congTyRepository.existsByMaSoThue(request.getMaSoThue())) {
            throw new BaseBusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Mã số thuế '" + request.getMaSoThue() + "' đã được đăng ký");
        }
        applyRequest(request, congTy);
        return mapToResponse(congTyRepository.save(congTy));
    }

    // ── B5 ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteCongTy(Long id) {
        log.info("B5: Xóa công ty ID: {}", id);
        CongTy congTy = findOrThrow(id);

        // Guard Clause: Không thể xóa cứng công ty khi vẫn còn ràng buộc FK trong DB
        if (!congTy.getNhaTuyenDungs().isEmpty() || !congTy.getTinTuyenDungs().isEmpty()) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR,
                    "Không thể xóa công ty vì vẫn còn dữ liệu nhà tuyển dụng hoặc tin tuyển dụng liên kết");
        }

        congTyRepository.delete(congTy);
        log.info("Xóa công ty thành công");
    }

    // ── B6 ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public boolean verifyMaSoThue(String maSoThue) {
        return congTyRepository.existsByMaSoThue(maSoThue);
    }

    // ── B7: Set ngành nghề (enum set) ─────────────────────────────────────────

    @Override
    @Transactional
    public CongTyResponse updateNganhNghes(Long congTyId, Set<NganhNgheEnum> nganhNghes, Long taiKhoanId) {
        log.info("B7: Cập nhật ngành nghề cho công ty {}", congTyId);
        verifyOwnership(taiKhoanId, congTyId);
        CongTy congTy = findOrThrow(congTyId);
        congTy.getNganhNghes().clear();
        congTy.getNganhNghes().addAll(nganhNghes);
        return mapToResponse(congTyRepository.save(congTy));
    }

    // ── B8: Lấy ngành nghề ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Set<NganhNgheEnum> getNganhNgheOfCongTy(Long congTyId) {
        // Copy ra HashSet mới: tránh LazyCollectionException khi Set bị serialize ngoài transaction
        return new java.util.HashSet<>(findOrThrow(congTyId).getNganhNghes());
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Long getCongTyIdByTaiKhoan(Long taiKhoanId) {
        return nhaTuyenDungRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.RECRUITER_NOT_FOUND))
                .getCongTy().getId();
    }

    /**
     * Guard: tìm CongTy hoặc throw lỗi chuẩn.
     */
    private CongTy findOrThrow(Long id) {
        return congTyRepository.findById(id)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy công ty ID: " + id));
    }

    /**
     * Guard: kiểm tra tài khoản có quyền sở hữu công ty không.
     * SRP – tách biệt authorization logic khỏi business logic.
     */
    private void verifyOwnership(Long taiKhoanId, Long congTyId) {
        NhaTuyenDung ntd = nhaTuyenDungRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.RECRUITER_NOT_FOUND));
        if (!ntd.getCongTy().getId().equals(congTyId)) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Bạn không có quyền thao tác với công ty này");
        }
    }

    private void applyRequest(CongTyRequest request, CongTy congTy) {
        congTy.setTenCongTy(request.getTenCongTy().trim());
        congTy.setMaSoThue(request.getMaSoThue());
        congTy.setLogoUrl(request.getLogoUrl());
        congTy.setWebsite(request.getWebsite());
        congTy.setMoTa(request.getMoTa());
    }

    private CongTyResponse mapToResponse(CongTy ct) {
        // Dùng count query thay vì lấy toàn bộ collection — tránh N+1 lazy load
        long soTinMo = congTyRepository.countActiveJobsByCongTyId(ct.getId());
        return CongTyResponse.builder()
                .id(ct.getId())
                .tenCongTy(ct.getTenCongTy())
                .maSoThue(ct.getMaSoThue())
                .logoUrl(ct.getLogoUrl())
                .website(ct.getWebsite())
                .moTa(ct.getMoTa())
                .ngayTao(ct.getNgayTao())
                .ngayCapNhat(ct.getNgayCapNhat())
                .nganhNghes(ct.getNganhNghes())
                .soTinDangMo(soTinMo)
                .build();
    }
}
