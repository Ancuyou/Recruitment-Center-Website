package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.ChiTietKyNangCvRequest;
import com.example.tuyendung.dto.response.ChiTietKyNangCvResponse;

import java.util.List;

/**
 * Service interface cho kỹ năng trong CV
 * Tuân thủ SOLID: Dependency Inversion
 */
public interface ChiTietKyNangCvService {

    /**
     * Thêm kỹ năng vào CV (C8 - phần kỹ năng)
     */
    ChiTietKyNangCvResponse addKyNangToCv(Long cvId, ChiTietKyNangCvRequest request);

    /**
     * Lấy danh sách kỹ năng của CV (C9 - phần kỹ năng)
     */
    List<ChiTietKyNangCvResponse> getKyNangByCvId(Long cvId);

    /**
     * Cập nhật mức thành thạo kỹ năng
     */
    ChiTietKyNangCvResponse updateKyNangInCv(Long cvId, Long kyNangId, ChiTietKyNangCvRequest request);

    /**
     * Xóa kỹ năng khỏi CV (C11 - phần kỹ năng)
     */
    void deleteKyNangFromCv(Long cvId, Long kyNangId);
}
