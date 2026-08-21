package com.parking.admin.service;

import com.parking.admin.dto.GateStatusResponse;
import com.parking.admin.entity.Gate;

import java.util.List;

/**
 * 게이트별 장비 상태 조회. 장비 통신 자체는 {@link com.parking.admin.gateway.DeviceGateway} 뒤로 격리되며,
 * 이 서비스는 GATE 테이블에 저장된 최신 상태를 조회/변환만 한다(폴링·캐시 갱신은 이 태스크 범위 밖).
 * 근거: design.md [Screen 1] 모듈경계
 */
public interface GateStatusService {

    List<GateStatusResponse> getGates();

    GateStatusResponse toResponse(Gate gate);
}
