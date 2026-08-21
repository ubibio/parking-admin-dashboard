package com.parking.admin.service;

import com.parking.admin.dto.BarrierOpenRequest;
import com.parking.admin.dto.GateCommandResponse;
import com.parking.admin.dto.IntercomCallResponse;
import com.parking.admin.dto.ManualPaymentRequest;
import com.parking.admin.dto.ManualPaymentResponse;

/**
 * 제어 명령 검증(사유 필수·게이트 타입 등) → DeviceGateway 위임 → AuditLogService 기록.
 * 근거: design.md [Screen 1] 모듈경계, [Do NOT]
 */
public interface GateControlService {

    /** reasonCode/reasonText 미입력 시 400(사유 필수) */
    GateCommandResponse openBarrier(Long gateId, BarrierOpenRequest request);

    GateCommandResponse rebootLpr(Long gateId);

    /** AUDIT_LOG 기록 대상 아님(backend-schema.md §2 비고) */
    IntercomCallResponse callIntercom(Long gateId);

    /** EXIT 게이트만 허용, reasonCode/reasonText 미입력 시 400 */
    ManualPaymentResponse manualPayment(Long gateId, ManualPaymentRequest request);
}
