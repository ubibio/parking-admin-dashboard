package com.parking.admin.service;

import com.parking.admin.dto.SeasonPassNotificationRequest;
import com.parking.admin.dto.SeasonPassNotificationResponse;

/**
 * 알림톡/SMS 발송. 외부 채널은 MessagingGateway 인터페이스 뒤로 격리.
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassNotificationService
 */
public interface SeasonPassNotificationService {

    /** POST /api/season-passes/notifications — 관리자 수동/조건 트리거. requestedBy = 현재 세션 계정 */
    SeasonPassNotificationResponse notify(SeasonPassNotificationRequest request);

    /** SeasonPassExpiryScheduler 전용 — 배치 자동 발송. requestedBy = NULL, 이미 발송된(SENT) 대상은 건너뛴다 */
    void notifyExpiringSoonForSchedule(int dDay);
}
