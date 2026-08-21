package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * GET /api/dashboard/gates 목록 항목 + WebSocket /topic/dashboard/gates 페이로드(변경 게이트 1건, 동일 스키마).
 * 근거: design.md [Screen 1] 입출력계약, backend-schema.md §2 GATE
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateStatusResponse {

    private Long gateId;
    private String gateName;

    /** ENTRY / EXIT */
    private String gateType;

    /** OPEN / CLOSED / FAULT */
    private String barrierStatus;

    /** NORMAL / COMM_ERROR */
    private String lprStatus;

    /** PRE_PAY / EXIT_PAY — 없으면 null */
    private String payStationType;

    /** NORMAL / COMM_ERROR / PAPER_LOW — 없으면 null */
    private String payStationStatus;

    private String lastVehicleNo;
    private LocalDateTime lastEventAt;
}
