package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * GET /api/revenue/store-settlement 응답 항목(상가 1건).
 * billingAmount는 사용된 쿠폰의 discountAmount 합계와 동일하게 산출한다
 * (입점 상가가 쿠폰 할인분을 정산 청구받는다는 가정 — §Issues 참고).
 * 근거: design.md [Screen 3] 입출력계약, 완료조건("입점 상가 청구용 정산 보고서 데이터")
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSettlementItemResponse {

    private Long storeId;
    private String storeName;
    private int issuedCouponCount;
    private int usedCouponCount;
    private BigDecimal discountAmount;
    private BigDecimal billingAmount;
}
