package com.example.tuyendung.exception;

import com.example.tuyendung.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * 
 * SOLID - SRP: Centralized exception handling for all controllers
 * - Converts exceptions to consistent HTTP responses
 * - Logs exceptions for monitoring
 * - Maps HTTP status codes correctly
 * 
 * Exception Hierarchy:
 * - ApplicationException (base) → 400-500 status codes
 *   - ResourceNotFoundException → 404
 *   - UnauthorizedException → 403
 *   - DuplicateResourceException → 409
 *   - ValidationException → 400
 *   - FileStorageException → 400/500
 *   - BusinessException → 400 (legacy, for backward compat)
 * - other Spring/Java exceptions → mapped appropriately
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ApplicationException and all subclasses
     * Maps the exception's HTTP status code to response
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException ex) {
        int statusCode = ex.getHttpStatusCode();
        log.warn("ApplicationException [{}]: {}", statusCode, ex.getMessage());
        
        return ResponseEntity.status(statusCode)
                .body(ApiResponse.error(statusCode, ex.getMessage()));
    }

    /**
     * Legacy handler for BusinessException (extended from ApplicationException)
     * Kept for backward compatibility
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("BusinessException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatusCode())
                .body(ApiResponse.error(ex.getHttpStatusCode(), ex.getMessage()));
    }

    /**
     * Handle authentication failures (401 Unauthorized)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "Sai email hoặc mật khẩu"));
    }

    /**
     * Handle Jakarta Bean Validation errors (400 Bad Request)
     * Collects field-level validation errors from @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation failed with {} errors", errors.size());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "Dữ liệu không hợp lệ"));
    }

    /**
     * Catch-all handler for unexpected exceptions (500 Internal Server Error)
     * Logs the full exception for debugging
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected exception occurred", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "Lỗi máy chủ: " + ex.getMessage()));
    }
}
