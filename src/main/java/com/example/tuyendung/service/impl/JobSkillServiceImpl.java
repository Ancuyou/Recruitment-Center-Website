package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.JobSkillRequest;
import com.example.tuyendung.dto.response.JobSkillResponse;
import com.example.tuyendung.entity.CtKyNangTin;
import com.example.tuyendung.entity.KyNang;
import com.example.tuyendung.entity.TinTuyenDung;
import com.example.tuyendung.exception.DuplicateResourceException;
import com.example.tuyendung.exception.ResourceNotFoundException;
import com.example.tuyendung.repository.CtKyNangTinRepository;
import com.example.tuyendung.repository.KyNangRepository;
import com.example.tuyendung.repository.TinTuyenDungRepository;
import com.example.tuyendung.service.JobSkillService;
import com.example.tuyendung.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation cho Job Skills (E8-E10)
 * 
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

    private final TimeProvider timeProvider;
    private final CtKyNangTinRepository ctKyNangTinRepository;
    private final TinTuyenDungRepository tinTuyenDungRepository;
    private final KyNangRepository kyNangRepository;

    @Override
    @Transactional
    public JobSkillResponse addSkillToJob(Long jobId, JobSkillRequest request) {
        log.info("E8: Thêm kỽ năng {} vào job {}", request.getKyNangId(), jobId);

        // Verify job exists and not deleted
        TinTuyenDung job = tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("TinTuyenDung", jobId));

        // Verify skill exists
        KyNang kyNang = kyNangRepository.findById(request.getKyNangId())
                .orElseThrow(() -> new ResourceNotFoundException("KyNang", request.getKyNangId()));

        try {
            CtKyNangTin ctKyNangTin = CtKyNangTin.builder()
                    .tinTuyenDung(job)
                    .kyNang(kyNang)
                    .yeucau(request.getYeucau())
                    .moTa(request.getMoTa())
                    .ngayTao(timeProvider.getCurrentTimeMillis())
                    .daXoa(false)
                    .build();

            CtKyNangTin saved = ctKyNangTinRepository.save(ctKyNangTin);
            log.info("Thêm kỽ năng {} vào job {} thành công", request.getKyNangId(), jobId);

            return mapToResponse(saved, kyNang.getTenKyNang());
        } catch (DataIntegrityViolationException e) {
            log.warn("Kỽ năng {} đã tồn tại trong job {}", request.getKyNangId(), jobId);
            throw new DuplicateResourceException("Kỹ năng này đã tồn tại trong công việc");
        }
    }

    @Override
    public List<JobSkillResponse> getJobSkills(Long jobId) {
        log.info("E9: Lấy danh sách kỽ năng yêu cầu job {}", jobId);

        // Verify job exists
        tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("TinTuyenDung", jobId));

        List<CtKyNangTin> skills = ctKyNangTinRepository.findByJobIdAndNotDeleted(jobId);
        return skills.stream()
                .map(skill -> mapToResponse(skill, skill.getKyNang().getTenKyNang()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobSkillResponse updateJobSkillRequirement(Long jobId, Long skillId, JobSkillRequest request) {
        log.info("E10: Cập nhật kỽ năng {} trong job {}", skillId, jobId);

        // Verify job exists
        tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("TinTuyenDung", jobId));

        // Find the skill in job
        CtKyNangTin ctKyNangTin = ctKyNangTinRepository.findByJobIdAndKyNangId(jobId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ năng không tồn tại trong công việc này"));

        // Update fields
        ctKyNangTin.setYeucau(request.getYeucau());
        ctKyNangTin.setMoTa(request.getMoTa());
        ctKyNangTin.setNgayCapNhat(timeProvider.getCurrentTimeMillis());

        CtKyNangTin updated = ctKyNangTinRepository.save(ctKyNangTin);
        log.info("Cập nhật kỽ năng {} trong job {} thành công", skillId, jobId);

        return mapToResponse(updated, ctKyNangTin.getKyNang().getTenKyNang());
    }

    @Override
    @Transactional
    public void deleteJobSkill(Long jobId, Long skillId) {
        log.info("Xóa kỽ năng {} từ job {}", skillId, jobId);

        // Verify job exists
        tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("TinTuyenDung", jobId));

        // Find and soft-delete
        CtKyNangTin ctKyNangTin = ctKyNangTinRepository.findByJobIdAndKyNangId(jobId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ năng không tồn tại trong công việc này"));

        ctKyNangTin.setDaXoa(true);
        ctKyNangTin.setNgayCapNhat(timeProvider.getCurrentTimeMillis());
        ctKyNangTinRepository.save(ctKyNangTin);

        log.info("Xóa kỽ năng {} từ job {} thành công", skillId, jobId);
    }

    @Override
    public JobSkillResponse getJobSkillById(Long jobId, Long skillId) {
        log.info("Lấy chi tiết kỽ năng {} từ job {}", skillId, jobId);

        tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("TinTuyenDung", jobId));

        CtKyNangTin skill = ctKyNangTinRepository.findByJobIdAndKyNangId(jobId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ năng không tồn tại"));

        return mapToResponse(skill, skill.getKyNang().getTenKyNang());
    }

    @Override
    public boolean hasJobSkill(Long jobId, Long skillId) {
        return ctKyNangTinRepository.findByJobIdAndKyNangId(jobId, skillId).isPresent();
    }

    /**
     * Helper method: Map entity to DTO
     */
    private JobSkillResponse mapToResponse(CtKyNangTin entity, String skillName) {
        return JobSkillResponse.builder()
                .id(entity.getTinTuyenDung().getId()) // Use composite ID
                .jobId(entity.getTinTuyenDung().getId())
                .kyNangId(entity.getKyNang().getId())
                .tenKyNang(skillName)
                .yeucau(entity.getYeucau())
                .moTa(entity.getMoTa())
                .ngayTao(entity.getNgayTao())
                .build();
    }

}