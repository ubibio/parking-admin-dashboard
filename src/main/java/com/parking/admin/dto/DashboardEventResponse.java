package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * GET /api/dashboard/events 목록 항목 + WebSocket /topic/dashboard/events 페이로드(이벤트 1건, 동일 스키마).
 * vehicleNo가 null인 미인식 건의 "미인식*" 표기는 화면(frontend-expert) 책임.
 * 근거: design.md [Screen 1] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardEventResponse {

    private LocalDateTime occurredAt;
    private String gateName;

    /** 미인식 건은 null */
    private String vehicleNo;

    /** GENERAL / SEASON_PASS */
    private String entryType;

    /** NORMAL / SETTLED / PASS / WARNING */
    private String eventStatus;

    private String note;
}
