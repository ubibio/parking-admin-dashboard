package com.parking.admin.service;

import com.parking.admin.dto.AlertPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 이상 이벤트를 주기적으로 평가해 /topic/dashboard/alerts로 push한다.
 * 30초 주기는 임의값이다(design.md에 폴링 주기 명시 없음 — 가정, Issues 참고).
 * 실제 장비 이벤트 콜백이 없는 이 태스크 범위에서 알림 완료조건을 충족시키기 위한 최소 트리거.
 * 근거: design.md [Screen 1] [완료조건] 알림 바
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardAlertScheduler {

    private final DashboardAlertService dashboardAlertService;
    private final DashboardWebSocketPublisher dashboardWebSocketPublisher;

    @Scheduled(fixedDelay = 30000)
    public void publishAlerts() {
        List<AlertPayload> alerts = dashboardAlertService.evaluate();
        if (alerts.isEmpty()) {
            return;
        }
        log.info("[DashboardAlertScheduler :: publishAlerts] :: 알림 발생 => count={}", alerts.size());
        alerts.forEach(dashboardWebSocketPublisher::publishAlert);
    }
}
