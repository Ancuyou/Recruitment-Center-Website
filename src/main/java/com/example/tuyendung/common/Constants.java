package com.example.tuyendung.common;

public class Constants {

    public static final String API_PREFIX = "/api";
    public static final String AUTH_PREFIX = "/auth";

    // Account Status
    public static final Boolean ACCOUNT_ACTIVE = true;
    public static final Boolean ACCOUNT_INACTIVE = false;

    // Role — tên enum thực tế từ VaiTroTaiKhoan
    public static final String ROLE_UNG_VIEN      = "UNG_VIEN";
    public static final String ROLE_NHA_TUYEN_DUNG = "NHA_TUYEN_DUNG";
    public static final String ROLE_ADMIN          = "ADMIN";

    // SpEL expressions dùng trong @PreAuthorize
    public static final String ROLE_UV_EXPR            = "hasRole('UNG_VIEN')";
    public static final String ROLE_NTD_EXPR           = "hasRole('NHA_TUYEN_DUNG')";
    public static final String ROLE_ADMIN_EXPR         = "hasRole('ADMIN')";
    public static final String ROLE_NTD_OR_ADMIN_EXPR  = "hasAnyRole('NHA_TUYEN_DUNG', 'ADMIN')";

    private Constants() {
        // Prevent instantiation
    }
}
