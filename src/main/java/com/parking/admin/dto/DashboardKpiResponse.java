package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GET /api/dashboard/kpi 응답 + WebSocket /topic/dashboard/kpi 페이로드(동일 스키마).
 * 근거: design.md [Screen 1] 입출력계약, backend-schema.md §2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardKpiResponse {

    private int totalSpaceCount;
    private int occupiedCount;
    private double occupancyRate;

    /** NORMAL(<90%) / WARN(>=90%) / FULL(>=98%) */
    private String occupancyLevel;

    private int todayEntryCount;
    private int todayExitCount;
    private BigDecimal todayRevenueAmount;
    private LocalDateTime updatedAt;
}
