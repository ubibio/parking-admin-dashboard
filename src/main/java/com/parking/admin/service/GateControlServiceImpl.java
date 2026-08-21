package com.parking.admin.service;

import com.parking.admin.dto.AuditLogCommand;
import com.parking.admin.dto.BarrierOpenRequest;
import com.parking.admin.dto.GateCommandResponse;
import com.parking.admin.dto.GateStatusResponse;
import com.parking.admin.dto.IntercomCallResponse;
import com.parking.admin.dto.ManualPaymentRequest;
import com.parking.admin.dto.ManualPaymentResponse;
import com.parking.admin.entity.Gate;
import com.parking.admin.entity.ParkingRecord;
import com.parking.admin.gateway.DeviceGateway;
import com.parking.admin.repository.GateRepository;
import com.parking.admin.repository.ParkingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 근거: design.md [Screen 1] 모듈경계 GateControlService, backend-schema.md §2 API 비고
 * - barrier/open, manual-payment: 성공·실패 무관 AUDIT_LOG 기록(비기능요구사항 §3, backend-schema.md §1)
 * - intercom/call: AUDIT_LOG 대상 아님(backend-schema.md §2 비고)
 */
@Service
@RequiredArgsConstructor
public class GateControlServiceImpl implements GateControlService {

    private static final String COMMAND_STATUS_ACCEPTED = "ACCEPTED";
    private static final String COMMAND_STATUS_FAILED = "FAILED";
    private static final String TARGET_TYPE_GATE = "GATE";
    private static final String TARGET_TYPE_PARKING_RECORD = "PARKING_RECORD";
    private static final String GATE_TYPE_EXIT = "EXIT";

    private final GateRepository gateRepository;
    private final ParkingRecordRepository parkingRecordRepository;
    private final DeviceGateway deviceGateway;
    private final AuditLogService auditLogService;
    private final GateStatusService gateStatusService;
    private final DashboardKpiService dashboardKpiService;
    private final DashboardWebSocketPublisher dashboardWebSocketPublisher;

    @Override
    @Transactional
    public GateCommandResponse openBarrier(Long gateId, BarrierOpenRequest request) {
        requireReason(request == null ? null : request.getReasonCode(), request == null ? null : request.getReasonText());
        Gate gate = getGateOrThrow(gateId);

        Map<String, Object> before = snapshotGate(gate);
        boolean accepted = deviceGateway.openBarrier(gateId);
        String commandStatus = accepted ? COMMAND_STATUS_ACCEPTED : COMMAND_STATUS_FAILED;
        if (accepted) {
            gate.setBarrierStatus("OPEN");
            gate.setLastEventAt(LocalDateTime.now());
            gateRepository.save(gate);
        }
        Map<String, Object> after = snapshotGate(gate);

        auditLogService.record(AuditLogCommand.builder()
                .actionType("BARRIER_OPEN")
                .targetType(TARGET_TYPE_GATE)
                .targetId(String.valueOf(gateId))
                .before(before)
                .after(after)
                .reasonCode(request.getReasonCode())
                .reasonText(request.getReasonText())
                .commandStatus(commandStatus)
                .build());

        dashboardWebSocketPublisher.publishGate(gateStatusService.toResponse(gate));

        return GateCommandResponse.builder()
                .commandId(UUID.randomUUID().toString())
                .commandStatus(commandStatus)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public GateCommandResponse rebootLpr(Long gateId) {
        Gate gate = getGateOrThrow(gateId);

        Map<String, Object> before = snapshotGate(gate);
        boolean accepted = deviceGateway.rebootLpr(gateId);
        String commandStatus = accepted ? COMMAND_STATUS_ACCEPTED : COMMAND_STATUS_FAILED;
        if (accepted) {
            gate.setLprStatus("NORMAL");
            gateRepository.save(gate);
        }
        Map<String, Object> after = snapshotGate(gate);

        auditLogService.record(AuditLogCommand.builder()
                .actionType("LPR_REBOOT")
                .targetType(TARGET_TYPE_GATE)
                .targetId(String.valueOf(gateId))
                .before(before)
                .after(after)
                .commandStatus(commandStatus)
                .build());

        dashboardWebSocketPublisher.publishGate(gateStatusService.toResponse(gate));

        return GateCommandResponse.builder()
                .commandId(UUID.randomUUID().toString())
                .commandStatus(commandStatus)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public IntercomCallResponse callIntercom(Long gateId) {
        getGateOrThrow(gateId);
        String callId = deviceGateway.callIntercom(gateId);
        String commandStatus = StringUtils.hasText(callId) ? COMMAND_STATUS_ACCEPTED : COMMAND_STATUS_FAILED;
        return IntercomCallResponse.builder()
                .callId(callId)
                .commandStatus(commandStatus)
                .build();
    }

    @Override
    @Transactional
    public ManualPaymentResponse manualPayment(Long gateId, ManualPaymentRequest request) {
        requireReason(request == null ? null : request.getReasonCode(), request == null ? null : request.getReasonText());
        Gate gate = getGateOrThrow(gateId);
        if (!GATE_TYPE_EXIT.equals(gate.getGateType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요금수동수납은 출구 게이트에서만 가능합니다.");
        }
        ParkingRecord record = parkingRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "입출차 기록을 찾을 수 없습니다: " + request.getRecordId()));

        Map<String, Object> before = snapshotRecord(record);

        String commandStatus = COMMAND_STATUS_ACCEPTED;
        record.setSettlementStatus("SETTLED");
        if (record.getExitAt() == null) {
            record.setExitAt(LocalDateTime.now());
            record.setExitGateId(gateId);
        }
        parkingRecordRepository.save(record);

        gate.setLastVehicleNo(record.getVehicleNo());
        gate.setLastEventAt(LocalDateTime.now());
        gateRepository.save(gate);

        Map<String, Object> after = snapshotRecord(record);

        auditLogService.record(AuditLogCommand.builder()
                .actionType("MANUAL_PAYMENT")
                .targetType(TARGET_TYPE_PARKING_RECORD)
                .targetId(String.valueOf(record.getRecordId()))
                .before(before)
                .after(after)
                .reasonCode(request.getReasonCode())
                .reasonText(request.getReasonText())
                .commandStatus(commandStatus)
                .build());

        dashboardWebSocketPublisher.publishGate(gateStatusService.toResponse(gate));
        dashboardWebSocketPublisher.publishKpi(dashboardKpiService.getKpi());

        return ManualPaymentResponse.builder()
                .commandId(UUID.randomUUID().toString())
                .commandStatus(commandStatus)
                .settledAmount(request.getAmount())
                .build();
    }

    private void requireReason(String reasonCode, String reasonText) {
        if (!StringUtils.hasText(reasonCode) || !StringUtils.hasText(reasonText)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사유 입력은 필수입니다.");
        }
    }

    private Gate getGateOrThrow(Long gateId) {
        return gateRepository.findById(gateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게이트를 찾을 수 없습니다: " + gateId));
    }

    private Map<String, Object> snapshotGate(Gate gate) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("gateId", gate.getGateId());
        snapshot.put("barrierStatus", gate.getBarrierStatus());
        snapshot.put("lprStatus", gate.getLprStatus());
        snapshot.put("payStationStatus", gate.getPayStationStatus());
        return snapshot;
    }

    private Map<String, Object> snapshotRecord(ParkingRecord record) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("recordId", record.getRecordId());
        snapshot.put("settlementStatus", record.getSettlementStatus());
        snapshot.put("feeAmount", record.getFeeAmount());
        snapshot.put("exitAt", record.getExitAt());
        return snapshot;
    }
}
