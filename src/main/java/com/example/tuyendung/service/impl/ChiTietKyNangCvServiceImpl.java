package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.ChiTietKyNangCvRequest;
import com.example.tuyendung.dto.response.ChiTietKyNangCvResponse;
import com.example.tuyendung.entity.ChiTietKyNangCv;
import com.example.tuyendung.entity.HoSoCv;
import com.example.tuyendung.entity.KyNang;
import com.example.tuyendung.entity.id.ChiTietKyNangCvId;
import com.example.tuyendung.exception.BusinessException;
import com.example.tuyendung.repository.ChiTietKyNangCvRepository;
import com.example.tuyendung.repository.HoSoCvRepository;
import com.example.tuyendung.repository.KyNangRepository;
import com.example.tuyendung.service.ChiTietKyNangCvService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChiTietKyNangCvServiceImpl implements ChiTietKyNangCvService {

    // Dependency Injection (SOLID - Dependency Inversion)
    private final ChiTietKyNangCvRepository chiTietKyNangCvRepository;
    private final HoSoCvRepository hoSoCvRepository;
    private final KyNangRepository kyNangRepository;

    @Override
    @Transactional
    public ChiTietKyNangCvResponse addKyNangToCv(Long cvId, ChiTietKyNangCvRequest request) {
        log.info("Thêm kỹ năng vào CV ID: {}", cvId);

        // Tìm CV
        HoSoCv hoSoCv = hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BusinessException("CV không tồn tại"));

        // Tìm kỹ năng
        KyNang kyNang = kyNangRepository.findById(request.getKyNangId())
                .orElseThrow(() -> new BusinessException("Kỹ năng không tồn tại"));

        // Kiểm tra mức thành thạo hợp lệ (1-5) - redundant due to @Max validation but kept as defensive check
        if (request.getMucThanhThao() < 1 || request.getMucThanhThao() > 5) {
            throw new BusinessException("Mức thành thạo không hợp lệ (1-5)");
        }

        // Kiểm tra kỹ năng này đã được thêm chưa
        if (chiTietKyNangCvRepository.findByHoSoCvIdAndKyNangId(cvId, request.getKyNangId()).isPresent()) {
            throw new BusinessException("Kỹ năng này đã tồn tại trong CV");
        }

        // Tạo chi tiết kỹ năng CV
        ChiTietKyNangCv chiTietKyNangCv = new ChiTietKyNangCv();
        chiTietKyNangCv.setId(new ChiTietKyNangCvId(cvId, request.getKyNangId()));
        chiTietKyNangCv.setHoSoCv(hoSoCv);
        chiTietKyNangCv.setKyNang(kyNang);
        chiTietKyNangCv.setMucThanhThao(request.getMucThanhThao());

        try {
            ChiTietKyNangCv savedChiTiet = chiTietKyNangCvRepository.save(chiTietKyNangCv);
            log.info("Thêm kỹ năng vào CV thành công");
            return mapToResponse(savedChiTiet);
        } catch (DataIntegrityViolationException e) {
            // FIXED: Handle race condition where concurrent request inserted same skill
            log.warn("Kỹ năng đã được thêm bởi request khác (race condition caught)");
            throw new BusinessException("Kỹ năng này đã tồn tại trong CV");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietKyNangCvResponse> getKyNangByCvId(Long cvId) {
        log.info("Lấy danh sách kỹ năng của CV ID: {}", cvId);

        // Kiểm tra CV tồn tại
        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BusinessException("CV không tồn tại"));

        return chiTietKyNangCvRepository.findByHoSoCvId(cvId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChiTietKyNangCvResponse updateKyNangInCv(Long cvId, Long kyNangId, ChiTietKyNangCvRequest request) {
        log.info("Cập nhật kỹ năng ID: {} trong CV ID: {}", kyNangId, cvId);

        // Kiểm tra CV tồn tại
        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BusinessException("CV không tồn tại"));

        // Kiểm tra mức thành thạo hợp lệ (1-5)
        if (request.getMucThanhThao() < 1 || request.getMucThanhThao() > 5) {
            throw new BusinessException("Mức thành thạo không hợp lệ (1-5)");
        }

        // Tìm chi tiết kỹ năng CV
        ChiTietKyNangCv chiTietKyNangCv = chiTietKyNangCvRepository.findByHoSoCvIdAndKyNangId(cvId, kyNangId)
                .orElseThrow(() -> new BusinessException("Kỹ năng này không tồn tại trong CV"));

        // Cập nhật mức thành thạo
        chiTietKyNangCv.setMucThanhThao(request.getMucThanhThao());

        ChiTietKyNangCv updatedChiTiet = chiTietKyNangCvRepository.save(chiTietKyNangCv);
        log.info("Cập nhật kỹ năng thành công");

        return mapToResponse(updatedChiTiet);
    }

    @Override
    @Transactional
    public void deleteKyNangFromCv(Long cvId, Long kyNangId) {
        log.info("Xóa kỹ năng ID: {} khỏi CV ID: {}", kyNangId, cvId);

        // Kiểm tra CV tồn tại
        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BusinessException("CV không tồn tại"));

        // Tìm và xóa chi tiết kỹ năng CV
        ChiTietKyNangCv chiTietKyNangCv = chiTietKyNangCvRepository.findByHoSoCvIdAndKyNangId(cvId, kyNangId)
                .orElseThrow(() -> new BusinessException("Kỹ năng này không tồn tại trong CV"));

        chiTietKyNangCvRepository.delete(chiTietKyNangCv);
        log.info("Xóa kỹ năng khỏi CV thành công");
    }

    /**
     * Chuyển đổi entity sang response DTO
     */
    private ChiTietKyNangCvResponse mapToResponse(ChiTietKyNangCv chiTietKyNangCv) {
        return ChiTietKyNangCvResponse.builder()
                .kyNangId(chiTietKyNangCv.getKyNang().getId())
                .tenKyNang(chiTietKyNangCv.getKyNang().getTenKyNang())
                .mucThanhThao(chiTietKyNangCv.getMucThanhThao())
                .build();
    }
}
