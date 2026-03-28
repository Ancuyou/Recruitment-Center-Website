package com.example.tuyendung.controller;

import com.example.tuyendung.common.ApiResponse;
import com.example.tuyendung.entity.enums.KhuVucEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller cho Khu vực (B11-B12)
 *
 * B11: Trả về danh sách enum values (không cần DB)
 * B12: Không cần — danh sách khu vực đã cố định qua enum
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class KhuVucController {

    /**
     * B11: Danh sách tất cả khu vực có sẵn (từ enum, public)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<KhuVucItem>>> getAllKhuVuc() {
        List<KhuVucItem> list = Arrays.stream(KhuVucEnum.values())
                .map(e -> new KhuVucItem(e.name(), e.getLabel()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("OK", list));
    }

    public record KhuVucItem(String value, String label) {}
}
