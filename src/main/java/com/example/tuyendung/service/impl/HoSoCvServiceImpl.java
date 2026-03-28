package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.HoSoCvRequest;
import com.example.tuyendung.dto.response.HoSoCvResponse;
import com.example.tuyendung.entity.HoSoCv;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.exception.ResourceNotFoundException;
import com.example.tuyendung.exception.UnauthorizedException;
import com.example.tuyendung.repository.HoSoCvRepository;
import com.example.tuyendung.repository.UngVienRepository;
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

    @Override
    @Transactional
    public HoSoCvResponse createCv(Long ungVienId, HoSoCvRequest request) {
        log.info("Tạo CV mới cho ứng viên ID: {}", ungVienId);

        // Tìm ứng viên
        UngVien ungVien = ungVienRepository.findById(ungVienId)
                .orElseThrow(() -> new ResourceNotFoundException("UngVien", ungVienId));

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
            throw new ResourceNotFoundException("UngVien", ungVienId);
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
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", cvId));

        // Kiểm tra CV thuộc về ứng viên hiện tại (SOLID - OCPD)
        if (!ungVienId.equals(hoSoCv.getUngVien().getId())) {
            throw new UnauthorizedException("view", "CV");
        }

        return mapToResponse(hoSoCv);
    }

    @Override
    @Transactional
    public HoSoCvResponse updateCv(Long cvId, Long ungVienId, HoSoCvRequest request) {
        log.info("Cập nhật CV ID: {}", cvId);

        HoSoCv hoSoCv = hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", cvId));

        // Kiểm tra CV thuộc về ứng viên hiện tại (SOLID - OCP)
        if (!ungVienId.equals(hoSoCv.getUngVien().getId())) {
            throw new UnauthorizedException("edit", "CV");
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
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", cvId));

        // Kiểm tra CV thuộc về ứng viên hiện tại (SOLID - OCP)
        if (!ungVienId.equals(hoSoCv.getUngVien().getId())) {
            throw new UnauthorizedException("delete", "CV");
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
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", cvId));
        
        if (!ungVienId.equals(newDefaultCv.getUngVien().getId())) {
            throw new UnauthorizedException("set-default", "CV");
        }

        // FIXED: Atomic operations - both happen as single DB transaction
        // This prevents race condition where 2 concurrent requests can both set default
        hoSoCvRepository.updateSetAllDefaultToFalse(ungVienId);
        hoSoCvRepository.updateSetDefaultCvById(newDefaultCv.getId());

        log.info("Đặt CV chính thành công");
    }

    @Override
    @Transactional
    public HoSoCvResponse uploadCvFile(Long cvId, Long ungVienId, String fileUrl) {
        log.info("Upload file PDF cho CV ID: {}", cvId);

        HoSoCv hoSoCv = hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", cvId));

        // Kiểm tra CV thuộc về ứng viên hiện tại (SOLID - OCP)
        if (!ungVienId.equals(hoSoCv.getUngVien().getId())) {
            throw new UnauthorizedException("upload", "CV");
        }

        hoSoCv.setFileCvUrl(fileUrl);
        HoSoCv updatedCv = hoSoCvRepository.save(hoSoCv);

        log.info("Upload file thành công");
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
                // Để frontend fetch chi tiết riêng
                .build();
    }
}
