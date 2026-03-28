package com.example.tuyendung.common;

import com.example.tuyendung.exception.BusinessException;
import com.example.tuyendung.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility component for JWT token extraction
 * 
 * SOLID Principles:
 * - Single Responsibility: Only handles JWT token extraction
 * - Dependency Inversion: Uses JwtTokenProvider interface
 * 
 * This eliminates code duplication across controllers (40+ lines saved)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenExtractor {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Extract user ID from Authorization header
     * 
     * @param authorization Authorization header value (format: "Bearer {token}")
     * @return User ID from JWT token
     * @throws BusinessException if token is invalid or malformed
     */
    public Long extractUserIdFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException("Token không hợp lệ");
        }
        
        String token = authorization.substring("Bearer ".length());
        try {
            return jwtTokenProvider.getUserIdFromToken(token);
        } catch (Exception e) {
            log.error("Lỗi parse token", e);
            throw new BusinessException("Token không hợp lệ");
        }
    }
}
