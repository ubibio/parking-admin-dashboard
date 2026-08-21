package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * WebSocket /topic/dashboard/alerts 페이로드.
 * 근거: design.md [Screen 1] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertPayload {

    /** UNRECOGNIZED_VEHICLE / BARRIER_OPEN_TOO_LONG / PAY_STATION_PAPER_LOW */
    private String alertType;
    private Long gateId;
    private String message;
    private LocalDateTime occurredAt;
}
