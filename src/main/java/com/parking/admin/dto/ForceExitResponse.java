package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POST /api/parking-records/{recordId}/force-exit 응답.
 * finalFeeAmount = feeAmount - discountAmount (0 미만이면 0으로 클램프, 가정 — Issues 참고).
 * 근거: design.md [Screen 2] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForceExitResponse {

    private Long recordId;

    /** 강제 출차 시 ADJUSTED로 전이(backend-schema.md §3 상태 전이) */
    private String settlementStatus;

    private BigDecimal finalFeeAmount;

    private LocalDateTime updatedAt;
}
