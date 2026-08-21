package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POST /api/parking-records/{recordId}/force-exit 요청 바디.
 * exitAt 미입력 시 서버 현재 시각으로 처리. feeAmount/discountAmount 미입력 시 기존 값 유지.
 * reasonCode/reasonText 미입력 시 서비스 계층에서 400 처리(사유 필수).
 * 근거: design.md [Screen 2] 입출력계약, 완료조건("요금을 0원 조정하거나 할인 금액을 수동 적용")
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForceExitRequest {

    private LocalDateTime exitAt;

    private BigDecimal feeAmount;

    private BigDecimal discountAmount;

    private String reasonCode;

    private String reasonText;
}
