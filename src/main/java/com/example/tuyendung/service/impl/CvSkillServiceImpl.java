package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.request.CvSkillRequest;
import com.example.tuyendung.dto.response.CvSkillResponse;
import com.example.tuyendung.entity.ChiTietKyNangCv;
import com.example.tuyendung.entity.HoSoCv;
import com.example.tuyendung.entity.KyNang;
import com.example.tuyendung.entity.id.ChiTietKyNangCvId;
import com.example.tuyendung.exception.DuplicateResourceException;
import com.example.tuyendung.exception.ResourceNotFoundException;
import com.example.tuyendung.repository.ChiTietKyNangCvRepository;
import com.example.tuyendung.repository.HoSoCvRepository;
import com.example.tuyendung.repository.KyNangRepository;
import com.example.tuyendung.service.CvSkillService;
import com.example.tuyendung.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation cho CV Skills (E4-E7)
 * 
 * SOLID Principles Applied:
 * - Dependency Inversion: Depends on repository interfaces, not concrete classes
 * - Single Responsibility: Chỉ xử lý CV skills business logic
 * - Open/Closed: Easy to extend without modifying existing code
 * - Liskov Substitution: Can be replaced with any impl of CvSkillService
 * 
 * Design Patterns:
 * - Service Pattern: Encapsulates business logic
 * - DTO Pattern: Uses custom DTOs for requests/responses
 * - Repository Pattern: Data access abstraction
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CvSkillServiceImpl implements CvSkillService {

    private final TimeProvider timeProvider;
    private final ChiTietKyNangCvRepository chiTietKyNangCvRepository;
    private final HoSoCvRepository hoSoCvRepository;
    private final KyNangRepository kyNangRepository;

    @Override
    @Transactional
    public CvSkillResponse addSkillToCv(Long cvId, CvSkillRequest request) {
        log.info("E4: Thêm kỹ năng {} vào CV {}", request.getKyNangId(), cvId);

        // Verify CV exists and not deleted
        HoSoCv cv = hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", cvId));

        // Verify skill exists
        KyNang kyNang = kyNangRepository.findById(request.getKyNangId())
                .orElseThrow(() -> new ResourceNotFoundException("KyNang", request.getKyNangId()));

        // Check for duplicates using database constraint
        try {
            ChiTietKyNangCvId id = new ChiTietKyNangCvId(cvId, request.getKyNangId());
            ChiTietKyNangCv chiTietKyNangCv = ChiTietKyNangCv.builder()
                    .id(id)
                    .hoSoCv(cv)
                    .kyNang(kyNang)
                    .mucThanhThao(request.getMucThanhThao())
                    .moTa(request.getMoTa())
                    .ngayCapNhat(timeProvider.getCurrentTimeMillis())
                    .daXoa(false)
                    .build();

            ChiTietKyNangCv saved = chiTietKyNangCvRepository.save(chiTietKyNangCv);
            log.info("Thêm kỹ năng {} vào CV {} thành công", request.getKyNangId(), cvId);

            return mapToResponse(saved, kyNang.getTenKyNang());
        } catch (DataIntegrityViolationException e) {
            log.warn("Kỹ năng {} đã tồn tại trong CV {}", request.getKyNangId(), cvId);
            throw new DuplicateResourceException("Kỹ năng này đã tồn tại trong CV");
        }
    }

    @Override
    public List<CvSkillResponse> getCvSkills(Long cvId) {
        log.info("E5: Lấy danh sách kỹ năng CV {}", cvId);

        // Verify CV exists
        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", cvId));

        List<ChiTietKyNangCv> skills = chiTietKyNangCvRepository.findByHoSoCvId(cvId);
        return skills.stream()
                .filter(skill -> !Boolean.TRUE.equals(skill.getDaXoa()))
                .map(skill -> mapToResponse(skill, skill.getKyNang().getTenKyNang()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CvSkillResponse updateCvSkillProficiency(Long cvId, Long skillId, CvSkillRequest request) {
        log.info("E6: Cập nhật kỽ năng {} trong CV {}", skillId, cvId);

        // Verify CV exists
        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", cvId));

        // Find the skill in CV
        ChiTietKyNangCv chiTietKyNangCv = chiTietKyNangCvRepository.findByHoSoCvIdAndKyNangId(cvId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ năng không tồn tại trong CV này"));

        // Update fields
        chiTietKyNangCv.setMucThanhThao(request.getMucThanhThao());
        chiTietKyNangCv.setMoTa(request.getMoTa());
        chiTietKyNangCv.setNgayCapNhat(timeProvider.getCurrentTimeMillis());

        ChiTietKyNangCv updated = chiTietKyNangCvRepository.save(chiTietKyNangCv);
        log.info("Cập nhật kỽ năng {} trong CV {} thành công", skillId, cvId);

        return mapToResponse(updated, chiTietKyNangCv.getKyNang().getTenKyNang());
    }

    @Override
    @Transactional
    public void deleteCvSkill(Long cvId, Long skillId) {
        log.info("E7: Xóa kỽ năng {} từ CV {}", skillId, cvId);

        // Verify CV exists
        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", cvId));

        // Find and soft-delete
        ChiTietKyNangCv chiTietKyNangCv = chiTietKyNangCvRepository.findByHoSoCvIdAndKyNangId(cvId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ năng không tồn tại trong CV này"));

        chiTietKyNangCv.setDaXoa(true);
        chiTietKyNangCv.setNgayCapNhat(timeProvider.getCurrentTimeMillis());
        chiTietKyNangCvRepository.save(chiTietKyNangCv);

        log.info("Xóa kỽ năng {} từ CV {} thành công", skillId, cvId);
    }

    @Override
    public CvSkillResponse getCvSkillById(Long cvId, Long skillId) {
        log.info("Lấy chi tiết kỽ năng {} từ CV {}", skillId, cvId);

        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", cvId));

        ChiTietKyNangCv skill = chiTietKyNangCvRepository.findByHoSoCvIdAndKyNangId(cvId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ năng không tồn tại"));

        return mapToResponse(skill, skill.getKyNang().getTenKyNang());
    }

    @Override
    public boolean hasCvSkill(Long cvId, Long skillId) {
        return chiTietKyNangCvRepository.findByHoSoCvIdAndKyNangId(cvId, skillId).isPresent();
    }

    /**
     * Helper method: Map entity to DTO
     */
    private CvSkillResponse mapToResponse(ChiTietKyNangCv entity, String skillName) {
        return CvSkillResponse.builder()
                .id(entity.getHoSoCv().getId()) // Use composite ID
                .cvId(entity.getHoSoCv().getId())
                .kyNangId(entity.getKyNang().getId())
                .tenKyNang(skillName)
                .mucThanhThao(entity.getMucThanhThao())
                .moTa(entity.getMoTa())
                .ngayTao(entity.getNgayTao() != null ? entity.getNgayTao().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : System.currentTimeMillis())
                .build();
    }

}