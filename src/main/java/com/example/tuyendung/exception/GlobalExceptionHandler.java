package com.example.tuyendung.exception;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.common.HttpStatusCode;
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
 * Global Exception Handler – xử lý tập trung toàn bộ exception cho mọi controller.
 *
 * SOLID – SRP: Mỗi @ExceptionHandler đảm nhiệm một nhóm exception rõ ràng.
 *
 * Exception Hierarchy (hiện tại):
 *  - BaseBusinessException (chuẩn H5) → ErrorCode → HTTP status code động
 *  - BadCredentialsException           → 401 Unauthorized
 *  - MethodArgumentNotValidException   → 400 Bad Request (Bean Validation @Valid)
 *  - IllegalArgumentException          → 400 Bad Request (validation thủ công)
 *  - Exception (catch-all)             → 500 Internal Server Error
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * [H5] Handle BaseBusinessException – exception nghiệp vụ chuẩn hóa.
     * Chuyển đổi ErrorCode → HTTP status code và trả về ApiResponse nhất quán.
     * Tất cả ServiceImpl đều throw class này thay vì các exception class thủ công cũ.
     */
    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseBusinessException(BaseBusinessException ex) {
        int statusCode = ex.getErrorCode().getStatus().value();
        log.warn("BusinessException [{}] {}: {}", statusCode, ex.getErrorCode().name(), ex.getMessage());

        return ResponseEntity.status(statusCode)
                .body(ApiResponse.error(statusCode, ex.getMessage()));
    }

    /**
     * Handle authentication failures – sai email/mật khẩu (401 Unauthorized).
     * Spring Security ném BadCredentialsException trước khi vào service layer.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Xác thực thất bại: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatusCode.UNAUTHORIZED, "Sai email hoặc mật khẩu"));
    }

    /**
     * Handle Jakarta Bean Validation errors từ @Valid / @Validated (400 Bad Request).
     * Tổng hợp tất cả field-level errors vào một map để trả về client.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation thất bại với {} lỗi: {}", errors.size(), errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatusCode.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"));
    }

    /**
     * Handle IllegalArgumentException – validation thủ công không dùng @Valid (400 Bad Request).
     * Ví dụ: file upload validation trong FileStorageService.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatusCode.BAD_REQUEST, ex.getMessage()));
    }

    /**
     * Catch-all handler – các exception không lường trước (500 Internal Server Error).
     * Log đầy đủ stack trace để dễ debug nhưng KHÔNG expose chi tiết kỹ thuật ra client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Lỗi máy chủ không xác định", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(HttpStatusCode.INTERNAL_SERVER_ERROR,
                        "Lỗi máy chủ nội bộ. Vui lòng thử lại sau."));
    }
}
