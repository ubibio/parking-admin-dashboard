package com.parking.admin.service;

import com.parking.admin.dto.AuditLogCommand;

/**
 * 공용 감사 로그 기록. Screen 1~4가 공유(design.md `AuditLogService` (공용)).
 * append-only — 조회/수정 메서드는 두지 않는다(요구 범위 밖).
 */
public interface AuditLogService {

    void record(AuditLogCommand command);
}
