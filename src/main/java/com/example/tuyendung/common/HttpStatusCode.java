package com.example.tuyendung.common;

/**
 * HTTP Status Code Constants – tránh hard-coded status codes trong exception/service.
 * Giúp maintain dễ hơn và giảm magic numbers.
 *
 * SOLID - DRY: định nghĩa một lần, dùng ở mọi nơi.
 */
public class HttpStatusCode {
    
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int NO_CONTENT = 204;
    
    // 4xx Client Errors
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    
    // 5xx Server Errors
    public static final int INTERNAL_SERVER_ERROR = 500;
    
    private HttpStatusCode() {
        // Prevent instantiation
    }
}
