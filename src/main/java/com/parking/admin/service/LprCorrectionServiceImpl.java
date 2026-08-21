package com.parking.admin.service;

import com.parking.admin.dto.AuditLogCommand;
import com.parking.admin.dto.PlateCorrectionRequest;
import com.parking.admin.dto.PlateCorrectionResponse;
import com.parking.admin.entity.ParkingRecord;
import com.parking.admin.repository.ParkingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 근거: design.md [Screen 2] 모듈경계 LprCorrectionService, 입출력계약 PUT plate-correction
 * - reasonCode/reasonText 둘 다 필수(사유필수, GateControlServiceImpl과 동일 관례로 통일 — 가정, Issues 참고)
 * - 성공 시에만 AUDIT_LOG 기록(design.md "두 쓰기 API는 성공 시 AuditLog에 beforeJson/afterJson을 함께 저장")
 */
@Service
@RequiredArgsConstructor
public class LprCorrectionServiceImpl implements LprCorrectionService {

    private static final String ACTION_TYPE_PLATE_CORRECTION = "PLATE_CORRECTION";
    private static final String TARGET_TYPE_PARKING_RECORD = "PARKING_RECORD";
    private static final String COMMAND_STATUS_ACCEPTED = "ACCEPTED";
    private static final String RECORD_STATUS_UNRECOGNIZED = "UNRECOGNIZED";
    private static final String RECORD_STATUS_NORMAL = "NORMAL";
    private static final int VEHICLE_NO_SUFFIX_LENGTH = 4;

    private final ParkingRecordRepository parkingRecordRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public PlateCorrectionResponse correctPlate(Long recordId, PlateCorrectionRequest request) {
        requireReason(request == null ? null : request.getReasonCode(), request == null ? null : request.getReasonText());
        ParkingRecord record = getRecordOrThrow(recordId);

        Map<String, Object> before = snapshot(record);

        record.setVehicleNo(request.getVehicleNo());
        record.setVehicleNoSuffix(resolveSuffix(request.getVehicleNo()));
        record.setVehicleType(request.getVehicleType());
        record.setEntryAt(request.getEntryAt());
        if (RECORD_STATUS_UNRECOGNIZED.equals(record.getRecordStatus())) {
            record.setRecordStatus(RECORD_STATUS_NORMAL);
        }
        parkingRecordRepository.saveAndFlush(record);

        Map<String, Object> after = snapshot(record);

        auditLogService.record(AuditLogCommand.builder()
                .actionType(ACTION_TYPE_PLATE_CORRECTION)
                .targetType(TARGET_TYPE_PARKING_RECORD)
                .targetId(String.valueOf(recordId))
                .before(before)
                .after(after)
                .reasonCode(request.getReasonCode())
                .reasonText(request.getReasonText())
                .commandStatus(COMMAND_STATUS_ACCEPTED)
                .build());

        return PlateCorrectionResponse.builder()
                .recordId(record.getRecordId())
                .recordStatus(record.getRecordStatus())
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

    private String resolveSuffix(String vehicleNo) {
        if (!StringUtils.hasText(vehicleNo) || vehicleNo.length() < VEHICLE_NO_SUFFIX_LENGTH) {
            return null;
        }
        return vehicleNo.substring(vehicleNo.length() - VEHICLE_NO_SUFFIX_LENGTH);
    }

    private Map<String, Object> snapshot(ParkingRecord record) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("recordId", record.getRecordId());
        snapshot.put("vehicleNo", record.getVehicleNo());
        snapshot.put("vehicleType", record.getVehicleType());
        snapshot.put("entryAt", record.getEntryAt());
        snapshot.put("recordStatus", record.getRecordStatus());
        return snapshot;
    }
}
