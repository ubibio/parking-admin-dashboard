package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * GET /api/revenue/summary 응답.
 * refundAmount: PARKING_RECORD·STORE_DISCOUNT_COUPON 어디에도 결제취소/환불 이력 컬럼이 없어(§Issues 참고)
 * 항상 0으로 응답한다(추측 금지 — 부재 사실 표면화).
 * 근거: design.md [Screen 3] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueSummaryResponse {

    private BigDecimal totalFeeAmount;
    private BigDecimal totalDiscountAmount;
    private BigDecimal netPaidAmount;
    private BigDecimal unpaidAmount;
    private BigDecimal refundAmount;
}
