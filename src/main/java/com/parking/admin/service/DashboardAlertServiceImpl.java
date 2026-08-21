package com.parking.admin.service;

import com.parking.admin.dto.AlertPayload;
import com.parking.admin.repository.GateRepository;
import com.parking.admin.repository.ParkingRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 판정 규칙은 design.md에 구체 조건(임계 시간 등)이 명시되어 있지 않아 설정값으로 임시 처리했다(가정 — Issues 참고).
 * - BARRIER_OPEN_TOO_LONG: barrierStatus=OPEN 게이트 중 lastEventAt이 임계 시간 이전인 경우
 *   (스키마에 "차단기 개방 시각" 전용 컬럼이 없어 GATE.last_event_at을 근사값으로 사용)
 * - PAY_STATION_PAPER_LOW: payStationStatus=PAPER_LOW인 게이트 전량
 * - UNRECOGNIZED_VEHICLE: 최근 window 내 recordStatus=UNRECOGNIZED로 입차한 건
 * 중복 발송 방지(dedupe/ack) 저장소가 없어 평가 주기마다 조건에 해당하면 매번 재발행된다.
 * 근거: design.md [Screen 1] 모듈경계
 */
@Service
public class DashboardAlertServiceImpl implements DashboardAlertService {

    private static final String BARRIER_STATUS_OPEN = "OPEN";
    private static final String PAY_STATION_STATUS_PAPER_LOW = "PAPER_LOW";
    private static final String RECORD_STATUS_UNRECOGNIZED = "UNRECOGNIZED";

    private final GateRepository gateRepository;
    private final ParkingRecordRepository parkingRecordRepository;
    private final int barrierOpenTooLongMinutes;
    private final int unrecognizedVehicleWindowMinutes;

    public DashboardAlertServiceImpl(GateRepository gateRepository,
                                      ParkingRecordRepository parkingRecordRepository,
                                      @Value("${parking.alert.barrier-open-too-long-minutes:5}") int barrierOpenTooLongMinutes,
                                      @Value("${parking.alert.unrecognized-vehicle-window-minutes:10}") int unrecognizedVehicleWindowMinutes) {
        this.gateRepository = gateRepository;
        this.parkingRecordRepository = parkingRecordRepository;
        this.barrierOpenTooLongMinutes = barrierOpenTooLongMinutes;
        this.unrecognizedVehicleWindowMinutes = unrecognizedVehicleWindowMinutes;
    }

    @Override
    public List<AlertPayload> evaluate() {
        LocalDateTime now = LocalDateTime.now();
        List<AlertPayload> alerts = new ArrayList<>();

        gateRepository.findByBarrierStatus(BARRIER_STATUS_OPEN).forEach(gate -> {
            if (gate.getLastEventAt() != null
                    && gate.getLastEventAt().isBefore(now.minusMinutes(barrierOpenTooLongMinutes))) {
                alerts.add(AlertPayload.builder()
                        .alertType("BARRIER_OPEN_TOO_LONG")
                        .gateId(gate.getGateId())
                        .message(gate.getGateName() + " 차단기가 장시간 열려 있습니다.")
                        .occurredAt(now)
                        .build());
            }
        });

        gateRepository.findByPayStationStatus(PAY_STATION_STATUS_PAPER_LOW).forEach(gate ->
                alerts.add(AlertPayload.builder()
                        .alertType("PAY_STATION_PAPER_LOW")
                        .gateId(gate.getGateId())
                        .message(gate.getGateName() + " 정산기 용지가 부족합니다.")
                        .occurredAt(now)
                        .build()));

        parkingRecordRepository
                .findByRecordStatusAndEntryAtAfter(RECORD_STATUS_UNRECOGNIZED, now.minusMinutes(unrecognizedVehicleWindowMinutes))
                .forEach(record -> alerts.add(AlertPayload.builder()
                        .alertType("UNRECOGNIZED_VEHICLE")
                        .gateId(record.getEntryGateId())
                        .message("미인식 차량이 진입했습니다.")
                        .occurredAt(record.getEntryAt())
                        .build()));

        return alerts;
    }
}
