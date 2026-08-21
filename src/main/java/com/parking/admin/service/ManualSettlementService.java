package com.parking.admin.service;

import com.parking.admin.dto.ForceExitRequest;
import com.parking.admin.dto.ForceExitResponse;

/**
 * 요금 수동 조정 및 강제 출차.
 * 근거: design.md [Screen 2] 모듈경계 ManualSettlementService
 */
public interface ManualSettlementService {

    ForceExitResponse forceExit(Long recordId, ForceExitRequest request);
}
