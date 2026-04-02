package com.example.tuyendung.common;

import com.example.tuyendung.exception.BaseBusinessException;
import com.example.tuyendung.exception.ErrorCode;
import com.example.tuyendung.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility component for JWT token extraction.
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles JWT token extraction from Authorization header
 * - Dependency Inversion: Depends on JwtTokenProvider interface
 *
 * Eliminates code duplication across controllers (40+ lines saved).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenExtractor {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Trích xuất User ID từ Authorization header.
     *
     * @param authorization giá trị header (format: "Bearer {token}")
     * @return User ID từ JWT token
     * @throws BaseBusinessException nếu header thiếu, sai format, hoặc token không hợp lệ
     */
    public Long extractUserIdFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BaseBusinessException(ErrorCode.INVALID_TOKEN,
                    "Authorization header thiếu hoặc sai định dạng (yêu cầu: Bearer <token>)");
        }

        String token = authorization.substring("Bearer ".length());
        try {
            return jwtTokenProvider.getUserIdFromToken(token);
        } catch (Exception e) {
            log.warn("Không thể parse JWT token: {}", e.getMessage());
            throw new BaseBusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
