package com.example.tuyendung.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO cho thống kê tin tuyển dụng (B22)
 *
 * Trang thái đơn ứng tuyển: 1=Mới, 2=Review, 3=Phỏng vấn, 4=Offer, 5=Từ chối
 */
@Getter
@Builder
public class JobStatisticsResponse {

    private Long tinId;
    private String tieuDe;

    private long tongSoDon;
    private long soMoi;
    private long soReview;
    private long soPhongVan;
    private long soOffer;
    private long soTuChoi;
}
