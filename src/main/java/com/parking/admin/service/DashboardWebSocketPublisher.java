package com.parking.admin.service;

import com.parking.admin.dto.AlertPayload;
import com.parking.admin.dto.DashboardEventResponse;
import com.parking.admin.dto.DashboardKpiResponse;
import com.parking.admin.dto.GateStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * STOMP 토픽 브로드캐스트. 조회 API 응답과 동일한 페이로드 스키마를 그대로 push한다.
 * 근거: design.md [Screen 1] 모듈경계, 입출력계약("푸시 페이로드 스키마는 아래 조회 API 응답과 동일하게 유지")
 */
@Component
@RequiredArgsConstructor
public class DashboardWebSocketPublisher {

    private static final String TOPIC_KPI = "/topic/dashboard/kpi";
    private static final String TOPIC_GATES = "/topic/dashboard/gates";
    private static final String TOPIC_EVENTS = "/topic/dashboard/events";
    private static final String TOPIC_ALERTS = "/topic/dashboard/alerts";

    private final SimpMessagingTemplate messagingTemplate;

    public void publishKpi(DashboardKpiResponse kpi) {
        messagingTemplate.convertAndSend(TOPIC_KPI, kpi);
    }

    /** gates 토픽은 변경된 게이트 1건만 push한다(전체 목록 아님). */
    public void publishGate(GateStatusResponse gate) {
        messagingTemplate.convertAndSend(TOPIC_GATES, gate);
    }

    public void publishEvent(DashboardEventResponse event) {
        messagingTemplate.convertAndSend(TOPIC_EVENTS, event);
    }

    public void publishAlert(AlertPayload alert) {
        messagingTemplate.convertAndSend(TOPIC_ALERTS, alert);
    }
}
