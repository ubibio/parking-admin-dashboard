package com.parking.admin.service;

import com.parking.admin.dto.AlertPayload;

import java.util.List;

/**
 * 이상 이벤트(미인식 차량 진입 / 차단기 장시간 열림 / 정산기 용지 부족) 판정 및 알림 페이로드 생성.
 * 근거: design.md [Screen 1] 모듈경계, [완료조건], [상태값] alertType
 */
public interface DashboardAlertService {

    List<AlertPayload> evaluate();
}
