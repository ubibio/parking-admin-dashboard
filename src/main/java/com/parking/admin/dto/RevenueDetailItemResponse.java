package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * GET /api/revenue/details 응답 항목(일자/월/년 단위 1건).
 * periodKey 형식: DAILY=yyyy-MM-dd, MONTHLY=yyyy-MM, YEARLY=yyyy.
 * 근거: design.md [Screen 3] 입출력계약, 완료조건("일자별 상세 정산 내역")
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueDetailItemResponse {

    private String periodKey;
    private int entryCount;
    private int exitCount;
    private int settlementCount;
    private BigDecimal baseFeeAmount;
    private BigDecimal discountAmount;
    private BigDecimal cardAmount;
    private BigDecimal easyPayAmount;
}
