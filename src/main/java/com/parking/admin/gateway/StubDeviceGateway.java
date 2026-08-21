package com.parking.admin.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 실제 현장 장비 연동 이전의 시뮬레이션 구현체. 항상 성공(true/callId 발급)을 반환한다.
 * 실 장비 프로토콜(TCP/RS-485 등) 연동은 이 태스크 범위 밖 — 실제 배포 전 별도 구현으로 교체 필요.
 * 근거: brief 범위(Screen 1 API+WebSocket+명령 엔드포인트), design.md DeviceGateway 인터페이스 격리
 */
@Slf4j
@Component
public class StubDeviceGateway implements DeviceGateway {

    @Override
    public boolean openBarrier(Long gateId) {
        log.info("[StubDeviceGateway :: openBarrier] :: 시뮬레이션 개방 명령 전송 => gateId={}", gateId);
        return true;
    }

    @Override
    public boolean rebootLpr(Long gateId) {
        log.info("[StubDeviceGateway :: rebootLpr] :: 시뮬레이션 재부팅 명령 전송 => gateId={}", gateId);
        return true;
    }

    @Override
    public String callIntercom(Long gateId) {
        String callId = UUID.randomUUID().toString();
        log.info("[StubDeviceGateway :: callIntercom] :: 시뮬레이션 인터폰 연결 => gateId={}, callId={}", gateId, callId);
        return callId;
    }
}
