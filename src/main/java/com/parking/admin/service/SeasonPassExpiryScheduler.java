package com.parking.admin.service;

import com.parking.admin.entity.SeasonPass;
import com.parking.admin.repository.SeasonPassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 일 1회: (1) D-7/D-3 대상 알림 생성+발송, (2) validTo 경과 건 EXPIRED 전환,
 * (3) ACTIVE -> EXPIRING_SOON(D-7 이내 진입) 전환. 자체 비즈니스 로직 없이 발송(SeasonPassNotificationService)과
 * 상태 전환(본 스케줄러가 직접 SeasonPassRepository로 상태만 갱신)을 호출한다.
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassExpiryScheduler, [상태값] 상태 전이, 입출력계약 스케줄러 동작.
 * 실행 시각(새벽 3시)은 design.md에 명시가 없어 임의값이다(가정 — Issues 참고, DashboardAlertScheduler 임의 주기 관례와 동일).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeasonPassExpiryScheduler {

    private static final int D_DAY_SEVEN = 7;
    private static final int D_DAY_THREE = 3;
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_EXPIRING_SOON = "EXPIRING_SOON";
    private static final String STATUS_EXPIRED = "EXPIRED";

    private final SeasonPassRepository seasonPassRepository;
    private final SeasonPassNotificationService seasonPassNotificationService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void run() {
        seasonPassNotificationService.notifyExpiringSoonForSchedule(D_DAY_SEVEN);
        seasonPassNotificationService.notifyExpiringSoonForSchedule(D_DAY_THREE);

        transitionToExpiringSoon();
        transitionToExpired();
    }

    private void transitionToExpiringSoon() {
        LocalDate today = LocalDate.now();
        List<SeasonPass> targets = seasonPassRepository.findByPassStatusAndValidToBetween(
                STATUS_ACTIVE, today, today.plusDays(D_DAY_SEVEN));
        if (targets.isEmpty()) {
            return;
        }
        targets.forEach(pass -> pass.setPassStatus(STATUS_EXPIRING_SOON));
        seasonPassRepository.saveAll(targets);
        log.info("[SeasonPassExpiryScheduler :: transitionToExpiringSoon] :: 전환 완료 => count={}", targets.size());
    }

    private void transitionToExpired() {
        List<SeasonPass> targets = seasonPassRepository.findByValidToBeforeAndPassStatusNot(
                LocalDate.now(), STATUS_EXPIRED);
        if (targets.isEmpty()) {
            return;
        }
        targets.forEach(pass -> pass.setPassStatus(STATUS_EXPIRED));
        seasonPassRepository.saveAll(targets);
        log.info("[SeasonPassExpiryScheduler :: transitionToExpired] :: 전환 완료 => count={}", targets.size());
    }
}
