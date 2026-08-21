package com.parking.admin.gateway;

/**
 * 현장 장비(차단기·LPR·인터폰) 통신 창구. GateControlService는 이 인터페이스로만 장비에 접근한다.
 * 브라우저·컨트롤러가 장비 IP를 직접 호출하지 않도록 격리하는 경계.
 * 근거: design.md [Screen 1] 모듈경계 GateStatusService/GateControlService, [Do NOT]
 */
public interface DeviceGateway {

    /** 차단기 강제 개방 명령 전송. true=명령 접수 성공 */
    boolean openBarrier(Long gateId);

    /** LPR 카메라 원격 소프트 리셋 명령 전송. true=명령 접수 성공 */
    boolean rebootLpr(Long gateId);

    /** 현장 인터폰/비상폰 원격 통화 연결. 반환값은 통화 세션 식별자(callId) */
    String callIntercom(Long gateId);
}
