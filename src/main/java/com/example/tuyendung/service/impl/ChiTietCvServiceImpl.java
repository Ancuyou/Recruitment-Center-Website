package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.ChiTietCvRequest;
import com.example.tuyendung.dto.response.ChiTietCvResponse;
import com.example.tuyendung.entity.ChiTietCv;
import com.example.tuyendung.entity.HoSoCv;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.ChiTietCvRepository;
import com.example.tuyendung.repository.HoSoCvRepository;
import com.example.tuyendung.service.ChiTietCvService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChiTietCvServiceImpl implements ChiTietCvService {

    // Dependency Injection (SOLID - Dependency Inversion)
    private final ChiTietCvRepository chiTietCvRepository;
    private final HoSoCvRepository hoSoCvRepository;

    @Override
    @Transactional
    public ChiTietCvResponse addChiTietCv(Long cvId, ChiTietCvRequest request) {
        log.info("Thêm chi tiết CV cho CV ID: {}", cvId);

        // Tìm CV
        HoSoCv hoSoCv = hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Không tìm thấy hồ sơ CV ID: " + cvId));

        // Kiểm tra loại bản ghi hợp lệ (1: Học vấn, 2: Kinh nghiệm, 3: Chứng chỉ)
        if (request.getLoaiBanGhi() < 1 || request.getLoaiBanGhi() > 3) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR,
                    "Loại bản ghi không hợp lệ – chỉ chấp nhận 1 (Học vấn), 2 (Kinh nghiệm), 3 (Chứng chỉ)");
        }

        // Kiểm tra ngày hợp lệ
        if (request.getNgayKetThuc() != null
                && request.getNgayKetThuc().isBefore(request.getNgayBatDau())) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR,
                    "Ngày kết thúc không được sớm hơn ngày bắt đầu");
        }

        // Tạo chi tiết CV
        ChiTietCv chiTietCv = new ChiTietCv();
        chiTietCv.setHoSoCv(hoSoCv);
        chiTietCv.setLoaiBanGhi(request.getLoaiBanGhi());
        chiTietCv.setTenToChuc(request.getTenToChuc());
        chiTietCv.setChuyenNganhHoacViTri(request.getChuyenNganhHoacViTri());
        chiTietCv.setNgayBatDau(request.getNgayBatDau());
        chiTietCv.setNgayKetThuc(request.getNgayKetThuc());
        chiTietCv.setMoTaChiTiet(request.getMoTaChiTiet());

        ChiTietCv savedChiTiet = chiTietCvRepository.save(chiTietCv);
        log.info("Thêm chi tiết CV thành công với ID: {}", savedChiTiet.getId());

        return mapToResponse(savedChiTiet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietCvResponse> getChiTietCvList(Long cvId) {
        log.info("Lấy danh sách chi tiết CV ID: {}", cvId);

        // Kiểm tra CV tồn tại
        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Không tìm thấy hồ sơ CV ID: " + cvId));

        return chiTietCvRepository.findByHoSoCvId(cvId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChiTietCvResponse updateChiTietCv(Long cvId, Long chiTietCvId, ChiTietCvRequest request) {
        log.info("Cập nhật chi tiết CV ID: {} từ CV ID: {}", chiTietCvId, cvId);

        // Kiểm tra CV tồn tại
        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Không tìm thấy hồ sơ CV ID: " + cvId));

        // Tìm chi tiết CV
        ChiTietCv chiTietCv = chiTietCvRepository.findByIdAndHoSoCvId(chiTietCvId, cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy chi tiết CV ID: " + chiTietCvId));

        // Kiểm tra ngày hợp lệ
        if (request.getNgayKetThuc() != null
                && request.getNgayKetThuc().isBefore(request.getNgayBatDau())) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR,
                    "Ngày kết thúc không được sớm hơn ngày bắt đầu");
        }

        // Cập nhật
        chiTietCv.setLoaiBanGhi(request.getLoaiBanGhi());
        chiTietCv.setTenToChuc(request.getTenToChuc());
        chiTietCv.setChuyenNganhHoacViTri(request.getChuyenNganhHoacViTri());
        chiTietCv.setNgayBatDau(request.getNgayBatDau());
        chiTietCv.setNgayKetThuc(request.getNgayKetThuc());
        chiTietCv.setMoTaChiTiet(request.getMoTaChiTiet());

        ChiTietCv updatedChiTiet = chiTietCvRepository.save(chiTietCv);
        log.info("Cập nhật chi tiết CV thành công");

        return mapToResponse(updatedChiTiet);
    }

    @Override
    @Transactional
    public void deleteChiTietCv(Long cvId, Long chiTietCvId) {
        log.info("Xóa chi tiết CV ID: {} từ CV ID: {}", chiTietCvId, cvId);

        // Kiểm tra CV tồn tại
        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Không tìm thấy hồ sơ CV ID: " + cvId));

        // Tìm và xóa chi tiết CV
        ChiTietCv chiTietCv = chiTietCvRepository.findByIdAndHoSoCvId(chiTietCvId, cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy chi tiết CV ID: " + chiTietCvId));

        chiTietCvRepository.delete(chiTietCv);
        log.info("Xóa chi tiết CV thành công");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietCvResponse> getChiTietCvByType(Long cvId, Integer loaiBanGhi) {
        log.info("Lấy danh sách chi tiết CV loại {} từ CV ID: {}", loaiBanGhi, cvId);

        // Kiểm tra CV tồn tại
        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Không tìm thấy hồ sơ CV ID: " + cvId));

        // Kiểm tra loại bản ghi hợp lệ
        if (loaiBanGhi < 1 || loaiBanGhi > 3) {
            throw new BaseBusinessException(ErrorCode.VALIDATION_ERROR,
                    "Loại bản ghi không hợp lệ – chỉ chấp nhận 1 (Học vấn), 2 (Kinh nghiệm), 3 (Chứng chỉ)");
        }

        return chiTietCvRepository.findByHoSoCvIdAndLoaiBanGhi(cvId, loaiBanGhi).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi entity sang response DTO
     */
    private ChiTietCvResponse mapToResponse(ChiTietCv chiTietCv) {
        return ChiTietCvResponse.builder()
                .id(chiTietCv.getId())
                .loaiBanGhi(chiTietCv.getLoaiBanGhi())
                .tenToChuc(chiTietCv.getTenToChuc())
                .chuyenNganhHoacViTri(chiTietCv.getChuyenNganhHoacViTri())
                .ngayBatDau(chiTietCv.getNgayBatDau())
                .ngayKetThuc(chiTietCv.getNgayKetThuc())
                .moTaChiTiet(chiTietCv.getMoTaChiTiet())
                .ngayTao(chiTietCv.getNgayTao())
                .build();
    }
}
