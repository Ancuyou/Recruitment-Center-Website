package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.HoSoCvRequest;
import com.example.tuyendung.dto.response.HoSoCvResponse;

import java.util.List;

/**
 * Service interface cho CV
 * Tuân thủ SOLID: Dependency Inversion - chỉ export interface, không export implementation
 */
public interface HoSoCvService {

    /**
     * Tạo CV mới (C1)
     */
    HoSoCvResponse createCv(Long ungVienId, HoSoCvRequest request);

    /**
     * Lấy danh sách CV của ứng viên (C2)
     */
    List<HoSoCvResponse> getCvsByUngVienId(Long ungVienId);

    /**
     * Lấy chi tiết CV (C3)
     * @param cvId ID của CV
     * @param ungVienId ID của ứng viên (kiểm tra quyền hạn)
     */
    HoSoCvResponse getCvById(Long cvId, Long ungVienId);

    /**
     * Cập nhật CV (C4)
     * @param cvId ID của CV
     * @param ungVienId ID của ứng viên (kiểm tra quyền hạn)
     */
    HoSoCvResponse updateCv(Long cvId, Long ungVienId, HoSoCvRequest request);

    /**
     * Xóa mềm CV (C5)
     * @param cvId ID của CV
     * @param ungVienId ID của ứng viên (kiểm tra quyền hạn)
     */
    void softDeleteCv(Long cvId, Long ungVienId);

    /**
     * Đặt CV chính (C6)
     */
    void setDefaultCv(Long ungVienId, Long cvId);

    /**
     * Upload file PDF CV thực tế (C7)
     * [H8] Nhận byte[] + contentType từ Controller (MultipartFile.getBytes()),
     *      gọi FileStorageService để lưu local và trả về URL thực tế.
     */
    HoSoCvResponse uploadCvFile(Long cvId, Long ungVienId, byte[] fileBytes, String contentType, String originalFilename);
}
