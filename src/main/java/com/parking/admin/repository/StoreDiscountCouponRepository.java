package com.parking.admin.repository;

import com.parking.admin.entity.StoreDiscountCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 근거: backend-schema.md §4 STORE_DISCOUNT_COUPON. Screen 3 상가 정산 집계 전용(6-3).
 * store 단위 GROUP BY 집계는 StoreDiscountSettlementServiceImpl에서 이 두 조회 결과를 병합해 산출한다.
 */
public interface StoreDiscountCouponRepository extends JpaRepository<StoreDiscountCoupon, Long> {

    /** 기간 내 발급 건수 — 결과 행: [storeId, issuedCount] */
    @Query("SELECT c.storeId, COUNT(c) FROM StoreDiscountCoupon c "
            + "WHERE c.issuedAt BETWEEN :start AND :end GROUP BY c.storeId")
    List<Object[]> countIssuedGroupedByStore(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 기간 내 사용 건수·할인금액 합계 — 결과 행: [storeId, usedCount, discountAmountSum] */
    @Query("SELECT c.storeId, COUNT(c), COALESCE(SUM(c.discountAmount), 0) FROM StoreDiscountCoupon c "
            + "WHERE c.couponStatus = 'USED' AND c.usedAt BETWEEN :start AND :end GROUP BY c.storeId")
    List<Object[]> countUsedGroupedByStore(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
