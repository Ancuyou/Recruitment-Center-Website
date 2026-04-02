package com.example.tuyendung.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum tập trung toàn bộ mã lỗi (Error Codes) của ứng dụng để chuẩn hóa (H5 Refactoring)
 */
@Getter
public enum ErrorCode {
    // ── Not Found ────────────────────────────────────────────────────────────
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên"),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy đơn ứng tuyển"),
    INTERVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy lịch phỏng vấn"),
    CV_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ CV"),
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tin tuyển dụng"),
    SKILL_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy kỹ năng"),
    RECRUITER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy nhà tuyển dụng"),
    CANDIDATE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy ứng viên"),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo"),
    // ── Conflict ─────────────────────────────────────────────────────────────
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã được sử dụng"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "Dữ liệu đã tồn tại"),
    // ── Bad Request ───────────────────────────────────────────────────────────
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "Token không hợp lệ hoặc đã hết hạn"),
    INVALID_INTERVIEW_TIME(HttpStatus.BAD_REQUEST, "Thời gian phỏng vấn không hợp lệ (giờ kết thúc phải sau giờ bắt đầu)"),
    ACCOUNT_NOT_ACTIVATED(HttpStatus.BAD_REQUEST, "Tài khoản chưa được kích hoạt, vui lòng kiểm tra email"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    SALARY_RANGE_INVALID(HttpStatus.BAD_REQUEST, "Mức lương min không được lớn hơn max"),
    INVALID_SEARCH_KEYWORD(HttpStatus.BAD_REQUEST, "Từ khóa tìm kiếm không được để trống"),
    // ── Forbidden ─────────────────────────────────────────────────────────────
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa"),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "Không có quyền truy cập vào tài nguyên này"),
    // ── Server Error ──────────────────────────────────────────────────────────
    FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi trong quá trình lưu trữ file"),
    FILE_SECURITY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi bảo mật: không có quyền ghi/xóa file");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
