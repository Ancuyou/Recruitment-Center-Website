package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.KyNangRequest;
import com.example.tuyendung.dto.response.KyNangResponse;

import java.util.List;

/**
 * Service interface cho kỹ năng
 * Tuân thủ SOLID: Dependency Inversion, Single Responsibility
 */
public interface KyNangService {

    /**
     * Lấy danh sách tất cả kỹ năng (C12)
     */
    List<KyNangResponse> getAllKyNang();

    /**
     * Tạo kỹ năng mới (C13)
     */
    KyNangResponse createKyNang(KyNangRequest request);

    /**
     * Tìm kiếm kỹ năng theo từ khóa (C14)
     */
    List<KyNangResponse> searchKyNang(String keyword);

    /**
     * Lấy chi tiết kỹ năng theo ID
     */
    KyNangResponse getKyNangById(Long id);

    /**
     * Xóa kỹ năng
     */
    void deleteKyNang(Long id);

    /**
     * Cập nhật kỹ năng
     */
    KyNangResponse updateKyNang(Long id, KyNangRequest request);
}
