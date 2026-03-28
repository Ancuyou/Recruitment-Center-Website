package com.example.tuyendung.service;

import com.example.tuyendung.dto.request.JobSkillRequest;
import com.example.tuyendung.dto.response.JobSkillResponse;
import java.util.List;

/**
 * Service Interface cho Job Skills (E8-E10)
 * 
 * SOLID Principles:
 * - Dependency Inversion: Service depends on this interface
 * - Single Responsibility: Chỉ xử lý job skills business logic
 * - Interface Segregation: Minimal methods needed by clients
 */
public interface JobSkillService {

    /**
     * E8: Thêm kỹ năng yêu cầu vào Job
     */
    JobSkillResponse addSkillToJob(Long jobId, JobSkillRequest request);

    /**
     * E9: Lấy danh sách kỹ năng yêu cầu của Job
     */
    List<JobSkillResponse> getJobSkills(Long jobId);

    /**
     * E10: Cập nhật mức yêu cầu kỹ năng
     */
    JobSkillResponse updateJobSkillRequirement(Long jobId, Long skillId, JobSkillRequest request);

    /**
     * Xóa kỹ năng khỏi job
     */
    void deleteJobSkill(Long jobId, Long skillId);

    /**
     * Lấy chi tiết kỹ năng job
     */
    JobSkillResponse getJobSkillById(Long jobId, Long skillId);

    /**
     * Kiểm tra job có kỹ năng không
     */
    boolean hasJobSkill(Long jobId, Long skillId);

}