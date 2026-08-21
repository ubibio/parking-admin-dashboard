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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상가별 웹 할인쿠폰 발급/사용. Screen 3 상가 정산(store-settlement) 집계 원천.
 * FK는 Store/ParkingRecord와 동일하게 단순 Long 컬럼으로 두고 연관관계 매핑은 하지 않는다(기존 관례).
 * 근거: backend-schema.md §4 STORE_DISCOUNT_COUPON
 */
@Entity
@Table(name = "store_discount_coupon", indexes = {
        @Index(name = "idx_store_discount_coupon_store_id", columnList = "store_id"),
        @Index(name = "idx_store_discount_coupon_issued_at", columnList = "issued_at"),
        @Index(name = "idx_store_discount_coupon_used_at", columnList = "used_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreDiscountCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    /** 사용 시 연결. 발급만 되고 미사용이면 NULL */
    @Column(name = "record_id")
    private Long recordId;

    /** ISSUED / USED */
    @Column(name = "coupon_status", length = 10, nullable = false)
    private String couponStatus;

    /** 사용 시점 확정 금액 */
    @Column(name = "discount_amount", precision = 10, scale = 0)
    private BigDecimal discountAmount;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
