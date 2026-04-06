package com.example.tuyendung.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity cho kỹ năng yêu cầu trong job
 * Bảng: ct_ky_nang_tin
 *
 * SOLID Principles:
 * - Single Responsibility: Đại diện duy nhất cho một kỹ năng yêu cầu
 * - Dependency Inversion: Không phụ thuộc vào chi tiết thực thi
 *
 * [H1] Dùng @Getter @Setter thay @Data để tránh vòng lặp equals/hashCode vô hạn
 *      với JPA lazy-loaded relations (StackOverflowError khi serialize).
 * [H2] Dùng LocalDateTime + @CreationTimestamp/@UpdateTimestamp thay vì Long epoch
 *      để đồng nhất với tất cả entity khác trong hệ thống; dễ đọc và Jackson tự format.
 */
@Entity
@Table(name = "ct_ky_nang_tin", indexes = {
        @Index(name = "idx_tin_tuyendung_id", columnList = "tin_tuyendung_id"),
        @Index(name = "idx_ky_nang_id", columnList = "ky_nang_id"),
        @Index(name = "idx_unique_tin_skill", columnList = "tin_tuyendung_id, ky_nang_id", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietKyNangTin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tin_tuyendung_id", nullable = false)
    private TinTuyenDung tinTuyenDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ky_nang_id", nullable = false)
    private KyNang kyNang;

    /**
     * Mức yêu cầu kỹ năng (1-5)
     * 1 = Sơ cấp, 2 = Cơ bản, 3 = Trung bình, 4 = Nâng cao, 5 = Chuyên gia
     */
    @Column(name = "yeucau", nullable = false)
    private Integer yeucau;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @CreationTimestamp
    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private LocalDateTime ngayTao;

    @UpdateTimestamp
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    /** Soft delete flag */
    @Column(name = "da_xoa", nullable = false)
    @Builder.Default
    private Boolean daXoa = false;

}