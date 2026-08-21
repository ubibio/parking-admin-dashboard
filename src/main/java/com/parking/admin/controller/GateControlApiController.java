package com.parking.admin.controller;

import com.parking.admin.dto.BarrierOpenRequest;
import com.parking.admin.dto.GateCommandResponse;
import com.parking.admin.dto.IntercomCallResponse;
import com.parking.admin.dto.ManualPaymentRequest;
import com.parking.admin.dto.ManualPaymentResponse;
import com.parking.admin.service.GateControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 차단기 강제열림 / LPR 재부팅 / 통화 / 요금수동수납(명령 전용, 쓰기).
 * 명령 API는 SUPER_ADMIN 전용(brief Constraints). 사유 필수 검증·AuditLog 기록은 GateControlService에서 수행.
 * 근거: design.md [Screen 1] 모듈경계, [Do NOT], backend-schema.md §2 API
 */
@RestController
@RequestMapping("/api/dashboard/gates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class GateControlApiController {

    private final GateControlService gateControlService;

    @PostMapping("/{gateId}/barrier/open")
    public GateCommandResponse openBarrier(@PathVariable Long gateId, @RequestBody(required = false) BarrierOpenRequest request) {
        return gateControlService.openBarrier(gateId, request);
    }

    @PostMapping("/{gateId}/lpr/reboot")
    public GateCommandResponse rebootLpr(@PathVariable Long gateId) {
        return gateControlService.rebootLpr(gateId);
    }

    @PostMapping("/{gateId}/intercom/call")
    public IntercomCallResponse callIntercom(@PathVariable Long gateId) {
        return gateControlService.callIntercom(gateId);
    }

    @PostMapping("/{gateId}/manual-payment")
    public ManualPaymentResponse manualPayment(@PathVariable Long gateId, @RequestBody ManualPaymentRequest request) {
        return gateControlService.manualPayment(gateId, request);
    }
}
