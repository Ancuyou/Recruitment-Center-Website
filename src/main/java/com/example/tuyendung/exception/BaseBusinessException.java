package com.example.tuyendung.exception;

import lombok.Getter;

/**
 * Lớp Exception nền tảng để giải quyết tình trạng "Bùng nổ Exception" (H5 Refactoring).
 * Mọi lỗi nghiệp vụ (Business logic) sẽ throw lớp này kèm theo ErrorCode.
 */
@Getter
public class BaseBusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String customMessage;

    public BaseBusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public BaseBusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage != null ? customMessage : errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
}
