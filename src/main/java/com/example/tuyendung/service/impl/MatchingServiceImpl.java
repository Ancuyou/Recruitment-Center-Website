package com.example.tuyendung.service.impl;

import com.example.tuyendung.dto.response.CandidateSuggestionResponse;
import com.example.tuyendung.dto.response.JobSuggestionResponse;
import com.example.tuyendung.dto.response.MatchScoreResponse;
import com.example.tuyendung.entity.ChiTietKyNangCv;
import com.example.tuyendung.entity.CongTy;
import com.example.tuyendung.entity.CtKyNangTin;
import com.example.tuyendung.entity.HoSoCv;
import com.example.tuyendung.entity.TinTuyenDung;
import com.example.tuyendung.entity.UngVien;
import com.example.tuyendung.exception.ResourceNotFoundException;
import com.example.tuyendung.repository.*;
import com.example.tuyendung.service.MatchingService;
import com.example.tuyendung.strategy.SkillMatchingStrategy;
import com.example.tuyendung.util.TimeProvider;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingServiceImpl implements MatchingService {

    private final TimeProvider timeProvider;
    private final MatchingRepository matchingRepository;
    private final HoSoCvRepository hoSoCvRepository;
    private final TinTuyenDungRepository tinTuyenDungRepository;
    private final ChiTietKyNangCvRepository chiTietKyNangCvRepository;
    private final CtKyNangTinRepository ctKyNangTinRepository;
    
    @Qualifier("cosineSimilarityStrategy")
    private final SkillMatchingStrategy defaultStrategy;
    
    @Qualifier("keywordMatchingStrategy")
    private final SkillMatchingStrategy keywordStrategy;

    private static final Integer DEFAULT_LIMIT = 10;

    @Override
    public List<CandidateSuggestionResponse> suggestCandidatesForJob(Long jobId, Integer limit) {
        log.info("E1: Gợi ý ứng viên cho job {}", jobId);

        // Verify job exists
        TinTuyenDung job = tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("TinTuyenDung", jobId));

        Integer finalLimit = limit != null ? limit : DEFAULT_LIMIT;

        // Get job skills
        List<CtKyNangTin> jobSkills = ctKyNangTinRepository.findByJobIdWithKyNang(jobId);
        if (jobSkills.isEmpty()) {
            log.warn("Job {} không có kỽ năng yêu cầu", jobId);
            return new ArrayList<>();
        }

        // Find matching candidates
        List<Long> candidateCvIds = matchingRepository.findMatchingCandidatesByJobId(jobId);

        // Calculate scores for each candidate and limit results
        List<CandidateSuggestionResponse> suggestions = candidateCvIds.stream()
                .map(cvId -> calculateCandidateSuggestion(cvId, jobId, job, jobSkills))
                .sorted(Comparator.comparingDouble(CandidateSuggestionResponse::getMatchScore).reversed())
                .limit(finalLimit)
                .collect(Collectors.toList());

        log.info("Tìm được {} ứng viên phù hợp cho job {}", suggestions.size(), jobId);
        return suggestions;
    }

    @Override
    public List<JobSuggestionResponse> suggestJobsForCandidate(Long candidateId, Integer limit) {
        log.info("E2: Gợi ý công việc cho ứng viên {}", candidateId);

        // Verify candidate exists
        hoSoCvRepository.findByIdAndNotDeleted(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", candidateId));

        Integer finalLimit = limit != null ? limit : DEFAULT_LIMIT;

        // Get candidate's default CV
        HoSoCv cv = hoSoCvRepository.findDefaultCvByUngVienId(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Ứng viên không có CV chính"));

        // Get candidate's skills
        List<ChiTietKyNangCv> candidateSkills = chiTietKyNangCvRepository.findByHoSoCvId(cv.getId());
        if (candidateSkills.isEmpty()) {
            log.warn("CV {} không có kỽ năng", cv.getId());
            return new ArrayList<>();
        }

        // Find matching jobs
        List<Long> matchingJobIds = matchingRepository.findMatchingJobsByCvId(cv.getId());

        // Calculate scores for each job and limit results
        List<JobSuggestionResponse> suggestions = matchingJobIds.stream()
                .map(jobId -> calculateJobSuggestion(cv.getId(), jobId, candidateSkills))
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
                .orElseThrow(() -> new ResourceNotFoundException("HoSoCV", cvId));

        tinTuyenDungRepository.findByIdAndNotDeleted(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("TinTuyenDung", jobId));

        // Get skills
        List<ChiTietKyNangCv> cvSkills = chiTietKyNangCvRepository.findByHoSoCvId(cvId);
        List<CtKyNangTin> jobSkills = ctKyNangTinRepository.findByJobIdAndNotDeleted(jobId);

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
                        CtKyNangTin::getYeucau
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
            Long cvId, Long jobId, TinTuyenDung job, List<CtKyNangTin> jobSkills) {

        MatchScoreResponse matchScore = calculateMatchScore(cvId, jobId);
        HoSoCv cv = hoSoCvRepository.findById(cvId).orElse(null);

        if (cv == null) {
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
    private JobSuggestionResponse calculateJobSuggestion(Long cvId, Long jobId, List<ChiTietKyNangCv> cvSkills) {
        MatchScoreResponse matchScore = calculateMatchScore(cvId, jobId);
        TinTuyenDung job = tinTuyenDungRepository.findById(jobId).orElse(null);

        if (job == null) {
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
        if (strategyName != null && strategyName.contains("Keyword")) {
            return keywordStrategy;
        }
        return defaultStrategy; // Default: Cosine Similarity
    }

}