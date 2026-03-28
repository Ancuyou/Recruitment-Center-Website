package com.example.tuyendung.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho tính điểm match
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchScoreRequest {

    @NotNull(message = "ID CV không được để trống")
    @Positive(message = "ID CV phải > 0")
    private Long cvId;

    @NotNull(message = "ID job không được để trống")
    @Positive(message = "ID job phải > 0")
    private Long jobId;

}