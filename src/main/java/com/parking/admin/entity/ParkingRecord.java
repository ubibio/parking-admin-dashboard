package com.parking.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 입/출차 세션 원장. Screen 1 이벤트 피드 + Screen 2 목록 + Screen 3 집계 원천을 공용(단일 테이블).
 * FK는 Gate/Store와 동일하게 단순 Long 컬럼으로 두고 연관관계 매핑은 하지 않는다(기존 관례).
 * 근거: backend-schema.md §2 PARKING_RECORD (Screen 2·3 공유 전제로 전체 필드 포함)
 */
@Entity
@Table(name = "parking_record", indexes = {
        @Index(name = "idx_parking_record_vehicle_no_suffix", columnList = "vehicle_no_suffix"),
        @Index(name = "idx_parking_record_entry_at", columnList = "entry_at"),
        @Index(name = "idx_parking_record_exit_at", columnList = "exit_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "entry_gate_id", nullable = false)
    private Long entryGateId;

    @Column(name = "exit_gate_id")
    private Long exitGateId;

    /** 미인식 시 NULL */
    @Column(name = "vehicle_no", length = 20)
    private String vehicleNo;

    /** 뒤 4자리 검색용 */
    @Column(name = "vehicle_no_suffix", length = 4)
    private String vehicleNoSuffix;

    /** GENERAL / COMPACT / EV */
    @Column(name = "vehicle_type", length = 10)
    private String vehicleType;

    /** 정기권 매칭 시 값 존재 → entryType=SEASON_PASS 파생 근거 */
    @Column(name = "season_pass_id")
    private Long seasonPassId;

    @Column(name = "entry_at", nullable = false)
    private LocalDateTime entryAt;

    @Column(name = "exit_at")
    private LocalDateTime exitAt;

    /** NORMAL / UNRECOGNIZED / SEASON_PASS / MANUAL_EXIT */
    @Column(name = "record_status", length = 15, nullable = false)
    private String recordStatus;

    /** UNSETTLED / SETTLED / ADJUSTED */
    @Column(name = "settlement_status", length = 10, nullable = false)
    private String settlementStatus;

    @Column(name = "fee_amount", precision = 10, scale = 0)
    private BigDecimal feeAmount;

    @Column(name = "discount_amount", precision = 10, scale = 0)
    private BigDecimal discountAmount;

    /** CARD / EASY_PAY */
    @Column(name = "payment_method", length = 10)
    private String paymentMethod;

    /** KAKAO / NAVER / SAMSUNG */
    @Column(name = "easy_pay_provider", length = 10)
    private String easyPayProvider;

    /** 내부 저장 경로 — 응답에 직접 노출 금지(LprImageService가 토큰 URL로 변환, Screen 2 범위) */
    @Column(name = "full_image_path", length = 300)
    private String fullImagePath;

    @Column(name = "plate_crop_image_path", length = 300)
    private String plateCropImagePath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.recordStatus == null) {
            this.recordStatus = "NORMAL";
        }
        if (this.settlementStatus == null) {
            this.settlementStatus = "UNSETTLED";
        }
        if (this.discountAmount == null) {
            this.discountAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
