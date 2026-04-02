package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.KyNangRequest;
import com.example.tuyendung.dto.response.KyNangResponse;
import com.example.tuyendung.entity.KyNang;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.KyNangRepository;
import com.example.tuyendung.service.KyNangService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KyNangServiceImpl implements KyNangService {

    // Dependency Injection (SOLID - Dependency Inversion)
    private final KyNangRepository kyNangRepository;

    @Override
    @Transactional(readOnly = true)
    public List<KyNangResponse> getAllKyNang() {
        log.info("Lấy danh sách tất cả kỹ năng");

        return kyNangRepository.findAllOrderByName().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public KyNangResponse createKyNang(KyNangRequest request) {
        log.info("Tạo kỹ năng mới: {}", request.getTenKyNang());

        // Kiểm tra tên kỹ năng đã tồn tại (case-insensitive)
        if (kyNangRepository.existsByTenKyNangIgnoreCase(request.getTenKyNang())) {
            throw new BaseBusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Kỹ năng '" + request.getTenKyNang() + "' đã tồn tại");
        }

        // Tạo kỹ năng mới
        KyNang kyNang = new KyNang();
        kyNang.setTenKyNang(request.getTenKyNang().trim());

        KyNang savedKyNang = kyNangRepository.save(kyNang);
        log.info("Tạo kỹ năng thành công với ID: {}", savedKyNang.getId());

        return mapToResponse(savedKyNang);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KyNangResponse> searchKyNang(String keyword) {
        log.info("Tìm kiếm kỹ năng với từ khóa: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BaseBusinessException(ErrorCode.INVALID_SEARCH_KEYWORD);
        }

        return kyNangRepository.searchByKeyword(keyword.trim()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public KyNangResponse getKyNangById(Long id) {
        log.info("Lấy chi tiết kỹ năng ID: {}", id);

        KyNang kyNang = kyNangRepository.findById(id)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.SKILL_NOT_FOUND));

        return mapToResponse(kyNang);
    }

    @Override
    @Transactional
    public void deleteKyNang(Long id) {
        log.info("Xóa kỹ năng ID: {}", id);

        KyNang kyNang = kyNangRepository.findById(id)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.SKILL_NOT_FOUND));

        kyNangRepository.delete(kyNang);
        log.info("Xóa kỹ năng thành công");
    }

    @Override
    @Transactional
    public KyNangResponse updateKyNang(Long id, KyNangRequest request) {
        log.info("Cập nhật kỹ năng ID: {}", id);

        KyNang kyNang = kyNangRepository.findById(id)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.SKILL_NOT_FOUND));

        // Kiểm tra tên kỹ năng không trùng với kỹ năng khác
        if (!kyNang.getTenKyNang().equalsIgnoreCase(request.getTenKyNang())
                && kyNangRepository.existsByTenKyNangIgnoreCase(request.getTenKyNang())) {
            throw new BaseBusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Tên kỹ năng '" + request.getTenKyNang() + "' đã tồn tại");
        }

        kyNang.setTenKyNang(request.getTenKyNang().trim());

        KyNang updatedKyNang = kyNangRepository.save(kyNang);
        log.info("Cập nhật kỹ năng thành công");

        return mapToResponse(updatedKyNang);
    }

    /**
     * Chuyển đổi entity sang response DTO
     */
    private KyNangResponse mapToResponse(KyNang kyNang) {
        return KyNangResponse.builder()
                .id(kyNang.getId())
                .tenKyNang(kyNang.getTenKyNang())
                .ngayTao(kyNang.getNgayTao())
                .build();
    }
}
