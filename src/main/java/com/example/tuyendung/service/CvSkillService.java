package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.CvSkillRequest;
import com.example.tuyendung.dto.response.CvSkillResponse;
import java.util.List;

/**
 * Service Interface cho CV Skills (E4-E7)
 * 
 * SOLID Principles:
 * - Dependency Inversion: Service depends on this interface, not implementation
 * - Single Responsibility: Chỉ xứ lý CV skills business logic
 * - Interface Segregation: Clients chỉ phụ thuộc vào methods cần thiết
 */
public interface CvSkillService {

    /**
     * E4: Thêm kỹ năng vào CV
     */
    CvSkillResponse addSkillToCv(Long cvId, CvSkillRequest request);

    /**
     * E5: Lấy danh sách kỹ năng của CV
     */
    List<CvSkillResponse> getCvSkills(Long cvId);

    /**
     * E6: Cập nhật mức thành thạo kỹ năng
     */
    CvSkillResponse updateCvSkillProficiency(Long cvId, Long skillId, CvSkillRequest request);

    /**
     * E7: Xóa kỹ năng khỏi CV
     */
    void deleteCvSkill(Long cvId, Long skillId);

    /**
     * Lấy chi tiết kỹ năng
     */
    CvSkillResponse getCvSkillById(Long cvId, Long skillId);

    /**
     * Kiểm tra CV có kỹ năng không
     */
    boolean hasCvSkill(Long cvId, Long skillId);

}