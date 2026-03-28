package com.example.tuyendung.exception;

import lombok.Getter;

/**
 * BusinessException – Legacy exception class (kept for backward compatibility)
 * 
 * Should be replaced with more specific exception types:
 * - ValidationException for input validation errors
 * - ResourceNotFoundException for missing resources
 * - UnauthorizedException for permission issues
 * - DuplicateResourceException for duplicate entries
 * 
 * SOLID - SRP: Base for business logic errors (generic fallback)
 * 
 * Deprecation Path: Replace uses with specific exception subclasses during Phase 2
 */
@Getter
public class BusinessException extends ApplicationException {

    public BusinessException(String message) {
        super(400, message);
    }

    public BusinessException(int code, String message) {
        super(code, message);
    }
}
