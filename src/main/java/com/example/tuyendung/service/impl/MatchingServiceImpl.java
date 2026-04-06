package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.response.CandidateSuggestionResponse;
import com.example.tuyendung.dto.response.JobSuggestionResponse;
import com.example.tuyendung.dto.response.MatchScoreResponse;
import com.example.tuyendung.entity.ChiTietKyNangCv;
import com.example.tuyendung.entity.CongTy;
import com.example.tuyendung.entity.ChiTietKyNangTin;
import com.example.tuyendung.entity.HoSoCv;
import com.example.tuyendung.entity.TinTuyenDung;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.repository.*;
import com.example.tuyendung.service.MatchingService;
import com.example.tuyendung.strategy.SkillMatchingStrategy;
import com.example.tuyendung.util.TimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation cho Matching Algorithm (E1-E3)
 *
 * SOLID Principles Applied:
 * - Strategy Pattern: Uses pluggable SkillMatchingStrategy
 * - Dependency Inversion: Depends on strategy interface
 * - Single Responsibility: Orchestrates matching logic
 * - Open/Closed: Can add new strategies without modifying this
 *
 * Design Patterns:
 * - Strategy Pattern: Multiple matching algorithms
 * - Factory Pattern: Select strategy by name
 * - Service Pattern: Business logic encapsulation
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class MatchingServiceImpl implements MatchingService {

    private final TimeProvider timeProvider;
    private final MatchingRepository matchingRepository;
    private final HoSoCvRepository hoSoCvRepository;
    private final TinTuyenDungRepository tinTuyenDungRepository;
    private final ChiTietKyNangCvRepository chiTietKyNangCvRepository;
    private final CtKyNangTinRepository ctKyNangTinRepository;
    private final UngVienRepository ungVienRepository;

    @Qualifier("cosineSimilarityStrategy")
    private final SkillMatchingStrategy defaultStrategy;

    @Qualifier("keywordMatchingStrategy")
    private final SkillMatchingStrategy keywordStrategy;

    private static final Integer DEFAULT_LIMIT = 10;

    public MatchingServiceImpl(
            TimeProvider timeProvider,
            MatchingRepository matchingRepository,
            HoSoCvRepository hoSoCvRepository,
            TinTuyenDungRepository tinTuyenDungRepository,
            ChiTietKyNangCvRepository chiTietKyNangCvRepository,
            CtKyNangTinRepository ctKyNangTinRepository,
            UngVienRepository ungVienRepository,
            @Qualifier("cosineSimilarityStrategy") SkillMatchingStrategy defaultStrategy,
            @Qualifier("keywordMatchingStrategy") SkillMatchingStrategy keywordStrategy) {
        this.timeProvider = timeProvider;
        this.matchingRepository = matchingRepository;
        this.hoSoCvRepository = hoSoCvRepository;
        this.tinTuyenDungRepository = tinTuyenDungRepository;
        this.chiTietKyNangCvRepository = chiTietKyNangCvRepository;
        this.ctKyNangTinRepository = ctKyNangTinRepository;
        this.ungVienRepository = ungVienRepository;
        this.defaultStrategy = defaultStrategy;
        this.keywordStrategy = keywordStrategy;
    }

    @Override
    public List<CandidateSuggestionResponse> suggestCandidatesForJob(Long jobId, Integer limit) {
        log.info("E1: Gợi ý ứng viên cho job {}", jobId);

        // Verify job exists
        TinTuyenDung job = tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.JOB_NOT_FOUND,
                        "Không tìm thấy tin tuyển dụng ID: " + jobId));

        Integer finalLimit = limit != null ? limit : DEFAULT_LIMIT;

        // Get job skills
        List<ChiTietKyNangTin> jobSkills = ctKyNangTinRepository.findByJobIdWithKyNang(jobId);
        if (jobSkills.isEmpty()) {
            log.warn("Job {} chưa có kỹ năng yêu cầu – không thể gợi ý ứng viên", jobId);
            return new ArrayList<>();
        }

        // Find matching candidates
        List<Long> candidateCvIds = matchingRepository.findMatchingCandidatesByJobId(jobId);

        // Calculate scores for each candidate and limit results
        List<CandidateSuggestionResponse> suggestions = candidateCvIds.stream()
                .map(cvId -> calculateCandidateSuggestion(cvId, jobId, job, jobSkills))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(CandidateSuggestionResponse::getMatchScore).reversed())
                .limit(finalLimit)
                .collect(Collectors.toList());

        log.info("Tìm được {} ứng viên phù hợp cho job {}", suggestions.size(), jobId);
        return suggestions;
    }

    @Override
    public List<JobSuggestionResponse> suggestJobsForCandidate(Long candidateId, Integer limit) {
        log.info("E2: Gợi ý công việc cho ứng viên {}", candidateId);

        // Verify candidate exists (candidateId must be UngVien ID)
        UngVien candidate = ungVienRepository.findById(candidateId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CANDIDATE_NOT_FOUND,
                        "Không tìm thấy ứng viên ID: " + candidateId));

        Integer finalLimit = limit != null ? limit : DEFAULT_LIMIT;

        // Get candidate's default CV
        HoSoCv cv = hoSoCvRepository.findDefaultCvByUngVienId(candidate.getId())
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Ứng viên chưa có CV chính, vui lòng thiết lập CV chính trước"));

        // Get candidate's skills
        List<ChiTietKyNangCv> candidateSkills = chiTietKyNangCvRepository.findByHoSoCvId(cv.getId());
        if (candidateSkills.isEmpty()) {
            log.warn("CV {} chưa có kỹ năng – không thể gợi ý công việc", cv.getId());
            return new ArrayList<>();
        }

        // Find matching jobs
        List<Long> matchingJobIds = matchingRepository.findMatchingJobsByCvId(cv.getId());

        // Calculate scores for each job and limit results
        List<JobSuggestionResponse> suggestions = matchingJobIds.stream()
                .map(jobId -> calculateJobSuggestion(cv.getId(), jobId, candidateSkills))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(JobSuggestionResponse::getMatchScore).reversed())
                .limit(finalLimit)
                .collect(Collectors.toList());

        log.info("Tìm được {} công việc phù hợp cho ứng viên {}", suggestions.size(), candidateId);
        return suggestions;
    }

    @Override
    public MatchScoreResponse calculateMatchScore(Long cvId, Long jobId) {
        log.info("E3: Tính điểm match cho CV {} - Job {}", cvId, jobId);
        return calculateMatchScoreWithStrategy(cvId, jobId, defaultStrategy.getStrategyName());
    }

    @Override
    public MatchScoreResponse calculateMatchScoreWithStrategy(Long cvId, Long jobId, String strategyName) {
        log.info("Tính match score với strategy: {}", strategyName);

        // Verify CV and Job exist
        hoSoCvRepository.findByIdAndNotDeleted(cvId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.CV_NOT_FOUND,
                        "Không tìm thấy hồ sơ CV ID: " + cvId));

        tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.JOB_NOT_FOUND,
                        "Không tìm thấy tin tuyển dụng ID: " + jobId));

        // Get skills
        List<ChiTietKyNangCv> cvSkills = chiTietKyNangCvRepository.findByHoSoCvId(cvId);
        List<ChiTietKyNangTin> jobSkills = ctKyNangTinRepository.findByJobIdAndNotDeleted(jobId);

        // Build skill maps
        List<Long> cvSkillIds = cvSkills.stream()
                .map(s -> s.getKyNang().getId())
                .collect(Collectors.toList());

        Map<Long, Integer> cvSkillProficiency = cvSkills.stream()
                .collect(Collectors.toMap(
                        s -> s.getKyNang().getId(),
                        ChiTietKyNangCv::getMucThanhThao
                ));

        List<Long> jobSkillIds = jobSkills.stream()
                .map(s -> s.getKyNang().getId())
                .collect(Collectors.toList());

        Map<Long, Integer> jobSkillRequirements = jobSkills.stream()
                .collect(Collectors.toMap(
                        s -> s.getKyNang().getId(),
                        ChiTietKyNangTin::getYeucau
                ));

        // Select strategy
        SkillMatchingStrategy strategy = selectStrategy(strategyName);

        // Calculate score
        MatchScoreResponse response = strategy.calculateMatchScore(
                cvSkillIds,
                cvSkillProficiency,
                jobSkillIds,
                jobSkillRequirements
        );

        response.setCvId(cvId);
        response.setJobId(jobId);

        log.info("Match score: {} ({}%)", response.getMatchScore(), response.getMatchPercentage());
        return response;
    }

    /**
     * Helper: Calculate suggestion for candidate
     */
    private CandidateSuggestionResponse calculateCandidateSuggestion(
            Long cvId, Long jobId, TinTuyenDung job, List<ChiTietKyNangTin> jobSkills) {

        MatchScoreResponse matchScore = calculateMatchScore(cvId, jobId);
        HoSoCv cv = hoSoCvRepository.findById(cvId).orElse(null);

        if (cv == null) {
            log.warn("Bỏ qua CV {} – không tìm thấy trong DB", cvId);
            return null;
        }

        UngVien ungVien = cv.getUngVien();
        return CandidateSuggestionResponse.builder()
                .ungVienId(ungVien.getId())
                .hoTen(ungVien.getHoTen())
                .email(ungVien.getTaiKhoan().getEmail())
                .soDienThoai(ungVien.getSoDienThoai())
                .cvId(cvId)
                .tieuDeCv(cv.getTieuDeCv())
                .matchScore(matchScore.getMatchScore())
                .matchPercentage(matchScore.getMatchPercentage())
                .matchLevel(matchScore.getMatchLevel())
                .skillMatchCount(matchScore.getMatchedSkills().size())
                .totalSkillsRequired(jobSkills.size())
                .calculatedAt(timeProvider.getCurrentTimeMillis())
                .build();
    }

    /**
     * Helper: Calculate suggestion for job
     */
    private JobSuggestionResponse calculateJobSuggestion(Long cvId, Long jobId,
                                                          List<ChiTietKyNangCv> cvSkills) {
        MatchScoreResponse matchScore = calculateMatchScore(cvId, jobId);
        TinTuyenDung job = tinTuyenDungRepository.findById(jobId).orElse(null);

        if (job == null) {
            log.warn("Bỏ qua Job {} – không tìm thấy trong DB", jobId);
            return null;
        }

        CongTy congTy = job.getCongTy();
        return JobSuggestionResponse.builder()
                .jobId(jobId)
                .tenVitri(job.getTieuDe())
                .moTa(job.getMoTaCongViec())
                .congTyId(String.valueOf(congTy.getId()))
                .congTyName(congTy.getTenCongTy())
                .matchScore(matchScore.getMatchScore())
                .matchPercentage(matchScore.getMatchPercentage())
                .matchLevel(matchScore.getMatchLevel())
                .skillMatchCount(matchScore.getMatchedSkills().size())
                .totalSkillsInCv(cvSkills.size())
                .diaDiem(job.getDiaDiem())
                .calculatedAt(timeProvider.getCurrentTimeMillis())
                .build();
    }

    /**
     * Factory method: Select strategy by name
     */
    private SkillMatchingStrategy selectStrategy(String strategyName) {
        if (strategyName == null || strategyName.trim().isEmpty()) {
            return defaultStrategy;
        }

        String normalized = strategyName.trim().toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        if (normalized.equals("keyword")
                || normalized.equals("keyword_matching")
                || normalized.equals("simple_keyword_matching")) {
            return keywordStrategy;
        }

        if (normalized.equals("cosine")
                || normalized.equals("cosine_similarity")
                || normalized.equals("cosine_similarity_with_proficiency_weighting")) {
            return defaultStrategy;
        }

        throw new BaseBusinessException(
                ErrorCode.VALIDATION_ERROR,
                "Strategy khong hop le: " + strategyName
                        + ". Ho tro: cosine_similarity, keyword_matching"
        );
    }
}