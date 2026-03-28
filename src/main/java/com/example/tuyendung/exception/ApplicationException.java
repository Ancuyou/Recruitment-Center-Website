package com.example.tuyendung.exception;

/**
 * ApplicationException – Base exception cho ứng dụng
 * Extend RuntimeException để tự động rollback transaction
 * 
 * SOLID - SRP: Base class cho tất cả custom exceptions
 * 
 * Lợi ích:
 * - Unified exception handling approach
 * - Easy to catch all app-specific exceptions
 * - Consistent HTTP status code management
 */
public class ApplicationException extends RuntimeException {
    
    protected final int httpStatusCode;
    
    public ApplicationException(int httpStatusCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }
    
    public ApplicationException(int httpStatusCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }
    
    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
