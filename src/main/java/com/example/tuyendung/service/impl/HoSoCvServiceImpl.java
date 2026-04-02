package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.HoSoCvRequest;
import com.example.tuyendung.dto.response.HoSoCvResponse;
import com.example.tuyendung.entity.HoSoCv;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.HoSoCvRepository;
import com.example.tuyendung.repository.UngVienRepository;
import com.example.tuyendung.service.FileStorageService;
import com.example.tuyendung.service.HoSoCvService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoSoCvServiceImpl implements HoSoCvService {

    // Dependency Injection (SOLID - Dependency Inversion)
    private final HoSoCvRepository hoSoCvRepository;
    private final UngVienRepository ungVienRepository;
    // [H8] Inject FileStorageService để upload file thực tế thay vì nhận String URL giả lập từ client
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public HoSoCvResponse createCv(Long ungVienId, HoSoCvRequest request) {
        log.info("Tạo CV mới cho ứng viên ID: {}", ungVienId);

        // Tìm ứng viên
        UngVien ungVien = ungVienRepository.findById(ungVienId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CANDIDATE_NOT_FOUND,
                        "Không tìm thấy ứng viên ID: " + ungVienId));

        // Tạo CV mới
        HoSoCv hoSoCv = new HoSoCv();
        hoSoCv.setUngVien(ungVien);
        hoSoCv.setTieuDeCv(request.getTieuDeCv());
        hoSoCv.setMucTieuNgheNghiep(request.getMucTieuNgheNghiep());
        hoSoCv.setFileCvUrl(request.getFileCvUrl());
        hoSoCv.setLaCvChinh(false);
        hoSoCv.setDaXoa(false);

        HoSoCv savedCv = hoSoCvRepository.save(hoSoCv);
        log.info("Tạo CV thành công với ID: {}", savedCv.getId());

        return mapToResponse(savedCv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoSoCvResponse> getCvsByUngVienId(Long ungVienId) {
        log.info("Lấy danh sách CV cho ứng viên ID: {}", ungVienId);

        // Kiểm tra ứng viên tồn tại
        if (!ungVienRepository.existsById(ungVienId)) {
            throw new BaseBusinessException(ErrorCode.CANDIDATE_NOT_FOUND,
                    "Không tìm thấy ứng viên ID: " + ungVienId);
        }

        return hoSoCvRepository.findByUngVienIdAndNotDeleted(ungVienId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HoSoCvResponse getCvById(Long cvId, Long ungVienId) {
        log.info("Lấy chi tiết CV ID: {}", cvId);

        HoSoCv hoSoCv = hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Không tìm thấy hồ sơ CV ID: " + cvId));

        // Kiểm tra CV thuộc về ứng viên hiện tại (SOLID - SRP)
        if (!ungVienId.equals(hoSoCv.getUngVien().getId())) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Bạn không có quyền xem CV này");
        }

        return mapToResponse(hoSoCv);
    }

    @Override
    @Transactional
    public HoSoCvResponse updateCv(Long cvId, Long ungVienId, HoSoCvRequest request) {
        log.info("Cập nhật CV ID: {}", cvId);

        HoSoCv hoSoCv = hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Không tìm thấy hồ sơ CV ID: " + cvId));

        // Kiểm tra CV thuộc về ứng viên hiện tại (SOLID - OCP)
        if (!ungVienId.equals(hoSoCv.getUngVien().getId())) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Bạn không có quyền chỉnh sửa CV này");
        }

        hoSoCv.setTieuDeCv(request.getTieuDeCv());
        hoSoCv.setMucTieuNgheNghiep(request.getMucTieuNgheNghiep());
        if (request.getFileCvUrl() != null) {
            hoSoCv.setFileCvUrl(request.getFileCvUrl());
        }

        HoSoCv updatedCv = hoSoCvRepository.save(hoSoCv);
        log.info("Cập nhật CV thành công");

        return mapToResponse(updatedCv);
    }

    @Override
    @Transactional
    public void softDeleteCv(Long cvId, Long ungVienId) {
        log.info("Xóa mềm CV ID: {}", cvId);

        HoSoCv hoSoCv = hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Không tìm thấy hồ sơ CV ID: " + cvId));

        // Kiểm tra CV thuộc về ứng viên hiện tại (SOLID - OCP)
        if (!ungVienId.equals(hoSoCv.getUngVien().getId())) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Bạn không có quyền xóa CV này");
        }

        hoSoCv.setDaXoa(true);
        // Nếu CV này là CV chính, đặt lại để không ai là chính
        if (Boolean.TRUE.equals(hoSoCv.getLaCvChinh())) {
            hoSoCv.setLaCvChinh(false);
        }

        hoSoCvRepository.save(hoSoCv);
        log.info("Xóa mềm CV thành công");
    }

    @Override
    @Transactional
    public void setDefaultCv(Long ungVienId, Long cvId) {
        log.info("Đặt CV chính cho ứng viên ID: {} với CV ID: {}", ungVienId, cvId);

        // Kiểm tra CV tồn tại và thuộc về ứng viên
        HoSoCv newDefaultCv = hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Không tìm thấy hồ sơ CV ID: " + cvId));

        if (!ungVienId.equals(newDefaultCv.getUngVien().getId())) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Bạn không có quyền đặt CV này làm CV chính");
        }

        // FIXED: Atomic operations - both happen as single DB transaction
        // This prevents race condition where 2 concurrent requests can both set default
        hoSoCvRepository.updateSetAllDefaultToFalse(ungVienId);
        hoSoCvRepository.updateSetDefaultCvById(newDefaultCv.getId());

        log.info("Đặt CV chính thành công");
    }

    @Override
    @Transactional
    public HoSoCvResponse uploadCvFile(Long cvId, Long ungVienId, byte[] fileBytes,
                                       String contentType, String originalFilename) {
        log.info("[H8] Upload file PDF thực tế cho CV ID: {}", cvId);

        HoSoCv hoSoCv = hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Không tìm thấy hồ sơ CV ID: " + cvId));

        if (!ungVienId.equals(hoSoCv.getUngVien().getId())) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Bạn không có quyền upload file cho CV này");
        }

        // Gọi FileStorageService để lưu file địa phương (local) và nhận lại URL tương đối
        String savedUrl = fileStorageService.uploadFile(fileBytes, originalFilename, contentType);

        hoSoCv.setFileCvUrl(savedUrl);
        HoSoCv updatedCv = hoSoCvRepository.save(hoSoCv);

        log.info("Upload file thành công, URL: {}", savedUrl);
        return mapToResponse(updatedCv);
    }

    /**
     * Chuyển đổi entity sang response DTO
     */
    private HoSoCvResponse mapToResponse(HoSoCv hoSoCv) {
        return HoSoCvResponse.builder()
                .id(hoSoCv.getId())
                .tieuDeCv(hoSoCv.getTieuDeCv())
                .mucTieuNgheNghiep(hoSoCv.getMucTieuNgheNghiep())
                .fileCvUrl(hoSoCv.getFileCvUrl())
                .laCvChinh(hoSoCv.getLaCvChinh())
                .ngayTao(hoSoCv.getNgayTao())
                .ngayCapNhat(hoSoCv.getNgayCapNhat())
                .build();
    }
}
