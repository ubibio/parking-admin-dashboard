package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * POST /api/dashboard/gates/{gateId}/intercom/call 응답.
 * 근거: design.md [Screen 1] 입출력계약. AUDIT_LOG 기록 대상 아님(backend-schema.md §2 비고).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercomCallResponse {

    private String callId;

    /** ACCEPTED / FAILED */
    private String commandStatus;
}
