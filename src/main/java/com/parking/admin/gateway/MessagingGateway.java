package com.parking.admin.gateway;

/**
 * 알림톡/SMS 발송 창구. SeasonPassNotificationService는 이 인터페이스로만 외부 발송 채널에 접근한다.
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassNotificationService("외부 채널은 MessagingGateway 인터페이스 뒤로 격리")
 */
public interface MessagingGateway {

    /** 알림톡/SMS 발송. true=발송 접수 성공(SENT), false=실패(FAILED) */
    boolean send(String phone, String channel, String message);
}
