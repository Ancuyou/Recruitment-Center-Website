package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.JobSkillRequest;
import com.example.tuyendung.dto.response.JobSkillResponse;
import com.example.tuyendung.entity.id.ChiTietKyNangTin;
import com.example.tuyendung.entity.KyNang;
import com.example.tuyendung.entity.TinTuyenDung;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.CtKyNangTinRepository;
import com.example.tuyendung.repository.KyNangRepository;
import com.example.tuyendung.repository.TinTuyenDungRepository;
import com.example.tuyendung.service.JobSkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation cho Job Skills (E8-E10)
 * SOLID Principles Applied:
 * - Dependency Inversion: Depends on repository interfaces
 * - Single Responsibility: Chỉ xử lý job skills logic
 * - Consistent with CvSkillServiceImpl design
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobSkillServiceImpl implements JobSkillService {

    private final CtKyNangTinRepository ctKyNangTinRepository;
    private final TinTuyenDungRepository tinTuyenDungRepository;
    private final KyNangRepository kyNangRepository;

    @Override
    @Transactional
    public JobSkillResponse addSkillToJob(Long jobId, JobSkillRequest request) {
        log.info("E8: Thêm kỹ năng {} vào job {}", request.getKyNangId(), jobId);

        // Verify job exists and not deleted
        TinTuyenDung job = tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.JOB_NOT_FOUND,
                        "Không tìm thấy tin tuyển dụng ID: " + jobId));

        // Verify skill exists
        KyNang kyNang = kyNangRepository.findById(request.getKyNangId())
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.SKILL_NOT_FOUND,
                        "Không tìm thấy kỹ năng ID: " + request.getKyNangId()));

        try {
            ChiTietKyNangTin chiTietKyNangTin = ChiTietKyNangTin.builder()
                    .tinTuyenDung(job)
                    .kyNang(kyNang)
                    .yeucau(request.getYeucau())
                    .moTa(request.getMoTa())
                    .daXoa(false)
                    .build();

            ChiTietKyNangTin saved = ctKyNangTinRepository.save(chiTietKyNangTin);
            log.info("Thêm kỹ năng {} vào job {} thành công", request.getKyNangId(), jobId);

            return mapToResponse(saved, kyNang.getTenKyNang());
        } catch (DataIntegrityViolationException e) {
            log.warn("Kỹ năng {} đã tồn tại trong job {}", request.getKyNangId(), jobId);
            throw new BaseBusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Kỹ năng này đã tồn tại trong công việc");
        }
    }

    @Override
    public List<JobSkillResponse> getJobSkills(Long jobId) {
        log.info("E9: Lấy danh sách kỹ năng yêu cầu job {}", jobId);

        // Verify job exists
        tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.JOB_NOT_FOUND,
                        "Không tìm thấy tin tuyển dụng ID: " + jobId));

        List<ChiTietKyNangTin> skills = ctKyNangTinRepository.findByJobIdAndNotDeleted(jobId);
        return skills.stream()
                .map(skill -> mapToResponse(skill, skill.getKyNang().getTenKyNang()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobSkillResponse updateJobSkillRequirement(Long jobId, Long skillId, JobSkillRequest request) {
        log.info("E10: Cập nhật kỹ năng {} trong job {}", skillId, jobId);

        // Verify job exists
        tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.JOB_NOT_FOUND,
                        "Không tìm thấy tin tuyển dụng ID: " + jobId));

        // Find the skill in job
        ChiTietKyNangTin chiTietKyNangTin = ctKyNangTinRepository.findByJobIdAndKyNangId(jobId, skillId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.SKILL_NOT_FOUND,
                        "Kỹ năng không tồn tại trong công việc này"));

        // Update fields
        chiTietKyNangTin.setYeucau(request.getYeucau());
        chiTietKyNangTin.setMoTa(request.getMoTa());

        ChiTietKyNangTin updated = ctKyNangTinRepository.save(chiTietKyNangTin);
        log.info("Cập nhật kỹ năng {} trong job {} thành công", skillId, jobId);

        return mapToResponse(updated, chiTietKyNangTin.getKyNang().getTenKyNang());
    }

    @Override
    @Transactional
    public void deleteJobSkill(Long jobId, Long skillId) {
        log.info("Xóa kỹ năng {} từ job {}", skillId, jobId);

        // Verify job exists
        tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.JOB_NOT_FOUND,
                        "Không tìm thấy tin tuyển dụng ID: " + jobId));

        // Find and soft-delete
        ChiTietKyNangTin chiTietKyNangTin = ctKyNangTinRepository.findByJobIdAndKyNangId(jobId, skillId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.SKILL_NOT_FOUND,
                        "Kỹ năng không tồn tại trong công việc này"));

        chiTietKyNangTin.setDaXoa(true);
        ctKyNangTinRepository.save(chiTietKyNangTin);

        log.info("Xóa kỹ năng {} từ job {} thành công", skillId, jobId);
    }

    @Override
    public JobSkillResponse getJobSkillById(Long jobId, Long skillId) {
        log.info("Lấy chi tiết kỹ năng {} từ job {}", skillId, jobId);

        tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.JOB_NOT_FOUND,
                        "Không tìm thấy tin tuyển dụng ID: " + jobId));

        ChiTietKyNangTin skill = ctKyNangTinRepository.findByJobIdAndKyNangId(jobId, skillId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.SKILL_NOT_FOUND,
                        "Kỹ năng không tồn tại trong công việc này"));

        return mapToResponse(skill, skill.getKyNang().getTenKyNang());
    }

    @Override
    public boolean hasJobSkill(Long jobId, Long skillId) {
        return ctKyNangTinRepository.findByJobIdAndKyNangId(jobId, skillId).isPresent();
    }

    /**
     * Helper method: Map entity to DTO
     */
    private JobSkillResponse mapToResponse(ChiTietKyNangTin entity, String skillName) {
        return JobSkillResponse.builder()
                .id(entity.getTinTuyenDung().getId())
                .jobId(entity.getTinTuyenDung().getId())
                .kyNangId(entity.getKyNang().getId())
                .tenKyNang(skillName)
                .yeucau(entity.getYeucau())
                .moTa(entity.getMoTa())
                .ngayTao(entity.getNgayTao())
                .build();
    }
}