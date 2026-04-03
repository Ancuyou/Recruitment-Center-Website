package com.example.tuyendung.security;

import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.entity.enums.VaiTroTaiKhoan;
import com.example.tuyendung.util.TimeProvider;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final TimeProvider timeProvider;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private TaiKhoan extractTaiKhoan(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal == null) {
            throw new IllegalStateException("Authentication principal is null");
        }

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getTaiKhoan();
        }

        if (principal instanceof TaiKhoan taiKhoan) {
            return taiKhoan;
        }

        throw new IllegalStateException("Unsupported authentication principal type: " + principal.getClass().getName());
    }

    public String generateAccessToken(Authentication authentication) {
        TaiKhoan taiKhoan = extractTaiKhoan(authentication);

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(String.valueOf(taiKhoan.getId()))
                .claim("email", taiKhoan.getEmail())
                .claim("vaiTro", taiKhoan.getVaiTro().name())
                .claim("authorities", authorities)
                .issuedAt(new Date())
                .expiration(new Date(timeProvider.getCurrentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        TaiKhoan taiKhoan = extractTaiKhoan(authentication);

        return Jwts.builder()
                .subject(String.valueOf(taiKhoan.getId()))
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(timeProvider.getCurrentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    public VaiTroTaiKhoan getVaiTroFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return VaiTroTaiKhoan.valueOf(claims.get("vaiTro", String.class));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}