package com.parking.admin.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 실제 알림톡/SMS 연동 이전의 시뮬레이션 구현체. 항상 성공(true)을 반환한다.
 * 실 발송 연동(카카오 비즈니스/SMS 게이트웨이)은 이 태스크 범위 밖 — 실제 배포 전 별도 구현으로 교체 필요.
 * 근거: brief 범위(Screen 4 API+스케줄러), design.md MessagingGateway 인터페이스 격리(StubDeviceGateway와 동일 패턴)
 */
@Slf4j
@Component
public class StubMessagingGateway implements MessagingGateway {

    @Override
    public boolean send(String phone, String channel, String message) {
        log.info("[StubMessagingGateway :: send] :: 시뮬레이션 발송 => channel={}, phone={}", channel, maskForLog(phone));
        return true;
    }

    /** 로그에도 원본 연락처를 남기지 않는다(비기능요구사항 §2 마스킹 취지 준용) */
    private String maskForLog(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "****" + phone.substring(phone.length() - 4);
    }
}
