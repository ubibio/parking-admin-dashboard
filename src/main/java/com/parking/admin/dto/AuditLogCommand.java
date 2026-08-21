package com.parking.admin.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * AuditLogService.record() 입력 — actorId/actorIp는 서버(세션·요청)에서 산출하므로 포함하지 않는다.
 * 근거: backend-schema.md §1 AUDIT_LOG, design.md 비기능요구사항 §3
 */
@Getter
@Builder
public class AuditLogCommand {

    /** BARRIER_OPEN / LPR_REBOOT / MANUAL_PAYMENT / PLATE_CORRECTION / FORCE_EXIT 등 */
    private final String actionType;

    /** GATE / PARKING_RECORD 등 */
    private final String targetType;

    private final String targetId;

    /** 변경 전 스냅샷 객체 — ObjectMapper로 직렬화됨. null 허용 */
    private final Object before;

    /** 변경 후 스냅샷 객체 — 명령 실패 시 null 허용 */
    private final Object after;

    private final String reasonCode;

    private final String reasonText;

    /** ACCEPTED / FAILED */
    private final String commandStatus;
}
