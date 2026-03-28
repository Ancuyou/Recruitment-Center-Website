package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.entity.enums.NganhNgheEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller cho Ngành nghề (B9-B10)
 *
 * B9: Trả về danh sách enum values (không cần DB)
 * B10: Không cần — danh sách ngành nghề đã cố định qua enum
 *      Admin thêm ngành nghề = thêm giá trị vào enum (code change)
 */
@RestController
@RequestMapping("/api/industries")
@RequiredArgsConstructor
public class NganhNgheController {

    /**
     * B9: Danh sách tất cả ngành nghề có sẵn (từ enum, public)
     * Trả về list object {name, label} để frontend dùng
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NganhNgheItem>>> getAllNganhNghe() {
        List<NganhNgheItem> list = Arrays.stream(NganhNgheEnum.values())
                .map(e -> new NganhNgheItem(e.name(), e.getLabel()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("OK", list));
    }

    /** Simple DTO inline — không cần file riêng */
    public record NganhNgheItem(String value, String label) {}
}
