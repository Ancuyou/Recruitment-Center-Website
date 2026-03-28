package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.ChiTietCvRequest;
import com.example.tuyendung.dto.response.ChiTietCvResponse;

import java.util.List;

/**
 * Service interface cho chi tiết CV (học vấn, kinh nghiệm, chứng chỉ)
 * Tuân thủ SOLID: Dependency Inversion
 */
public interface ChiTietCvService {

    /**
     * Thêm học vấn/KN (C8)
     */
    ChiTietCvResponse addChiTietCv(Long cvId, ChiTietCvRequest request);

    /**
     * Lấy danh sách chi tiết CV (C9)
     */
    List<ChiTietCvResponse> getChiTietCvList(Long cvId);

    /**
     * Cập nhật chi tiết CV (C10)
     */
    ChiTietCvResponse updateChiTietCv(Long cvId, Long chiTietCvId, ChiTietCvRequest request);

    /**
     * Xóa chi tiết CV (C11)
     */
    void deleteChiTietCv(Long cvId, Long chiTietCvId);

    /**
     * Lấy danh sách chi tiết theo loại bản ghi
     */
    List<ChiTietCvResponse> getChiTietCvByType(Long cvId, Integer loaiBanGhi);
}
