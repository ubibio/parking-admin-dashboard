package com.parking.admin.service;

import com.parking.admin.dto.AuditLogCommand;
import com.parking.admin.dto.ForceExitRequest;
import com.parking.admin.dto.ForceExitResponse;
import com.parking.admin.entity.ParkingRecord;
import com.parking.admin.repository.ParkingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 근거: design.md [Screen 2] 모듈경계 ManualSettlementService, 입출력계약 POST force-exit,
 * 상태 전이 "* --(강제출차)--> MANUAL_EXIT + settlementStatus=ADJUSTED"
 * - reasonCode/reasonText 둘 다 필수(사유필수, GateControlServiceImpl과 동일 관례로 통일 — 가정, Issues 참고)
 * - 성공 시에만 AUDIT_LOG 기록
 * - exitGateId는 요청 계약에 없어 변경하지 않는다(가정 — Issues 참고)
 */
@Service
@RequiredArgsConstructor
public class ManualSettlementServiceImpl implements ManualSettlementService {

    private static final String ACTION_TYPE_FORCE_EXIT = "FORCE_EXIT";
    private static final String TARGET_TYPE_PARKING_RECORD = "PARKING_RECORD";
    private static final String COMMAND_STATUS_ACCEPTED = "ACCEPTED";
    private static final String RECORD_STATUS_MANUAL_EXIT = "MANUAL_EXIT";
    private static final String SETTLEMENT_STATUS_ADJUSTED = "ADJUSTED";

    private final ParkingRecordRepository parkingRecordRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ForceExitResponse forceExit(Long recordId, ForceExitRequest request) {
        requireReason(request == null ? null : request.getReasonCode(), request == null ? null : request.getReasonText());
        ParkingRecord record = getRecordOrThrow(recordId);

        Map<String, Object> before = snapshot(record);

        record.setExitAt(request.getExitAt() != null ? request.getExitAt() : LocalDateTime.now());
        if (request.getFeeAmount() != null) {
            record.setFeeAmount(request.getFeeAmount());
        }
        if (request.getDiscountAmount() != null) {
            record.setDiscountAmount(request.getDiscountAmount());
        }
        record.setRecordStatus(RECORD_STATUS_MANUAL_EXIT);
        record.setSettlementStatus(SETTLEMENT_STATUS_ADJUSTED);
        parkingRecordRepository.saveAndFlush(record);

        Map<String, Object> after = snapshot(record);

        auditLogService.record(AuditLogCommand.builder()
                .actionType(ACTION_TYPE_FORCE_EXIT)
                .targetType(TARGET_TYPE_PARKING_RECORD)
                .targetId(String.valueOf(recordId))
                .before(before)
                .after(after)
                .reasonCode(request.getReasonCode())
                .reasonText(request.getReasonText())
                .commandStatus(COMMAND_STATUS_ACCEPTED)
                .build());

        return ForceExitResponse.builder()
                .recordId(record.getRecordId())
                .settlementStatus(record.getSettlementStatus())
                .finalFeeAmount(resolveFinalFeeAmount(record))
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private void requireReason(String reasonCode, String reasonText) {
        if (!StringUtils.hasText(reasonCode) || !StringUtils.hasText(reasonText)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사유 입력은 필수입니다.");
        }
    }

    private ParkingRecord getRecordOrThrow(Long recordId) {
        return parkingRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "입출차 기록을 찾을 수 없습니다: " + recordId));
    }

    /** feeAmount - discountAmount, 0 미만이면 0으로 클램프(가정 — Issues 참고) */
    private BigDecimal resolveFinalFeeAmount(ParkingRecord record) {
        BigDecimal fee = record.getFeeAmount() != null ? record.getFeeAmount() : BigDecimal.ZERO;
        BigDecimal discount = record.getDiscountAmount() != null ? record.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal result = fee.subtract(discount);
        return result.max(BigDecimal.ZERO);
    }

    private Map<String, Object> snapshot(ParkingRecord record) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("recordId", record.getRecordId());
        snapshot.put("exitAt", record.getExitAt());
        snapshot.put("feeAmount", record.getFeeAmount());
        snapshot.put("discountAmount", record.getDiscountAmount());
        snapshot.put("recordStatus", record.getRecordStatus());
        snapshot.put("settlementStatus", record.getSettlementStatus());
        return snapshot;
    }
}
