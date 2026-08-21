package com.parking.admin.service;

import com.parking.admin.dto.DashboardEventResponse;
import com.parking.admin.dto.TrafficResponse;
import com.parking.admin.entity.Gate;
import com.parking.admin.entity.ParkingRecord;
import com.parking.admin.repository.GateRepository;
import com.parking.admin.repository.ParkingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingEventLogServiceImpl implements ParkingEventLogService {

    private final ParkingRecordRepository parkingRecordRepository;
    private final GateRepository gateRepository;

    @Override
    public List<DashboardEventResponse> getRecentEvents(int limit) {
        Map<Long, String> gateNames = gateRepository.findAll().stream()
                .collect(Collectors.toMap(Gate::getGateId, Gate::getGateName));

        List<DashboardEventResponse> occurrences = new ArrayList<>();

        parkingRecordRepository.findByOrderByEntryAtDesc(PageRequest.of(0, limit))
                .forEach(record -> occurrences.add(toEntryOccurrence(record, gateNames)));

        parkingRecordRepository.findByExitAtIsNotNullOrderByExitAtDesc(PageRequest.of(0, limit))
                .forEach(record -> occurrences.add(toExitOccurrence(record, gateNames)));

        return occurrences.stream()
                .sorted(Comparator.comparing(DashboardEventResponse::getOccurredAt).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public List<TrafficResponse> getTraffic(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        int[] entryByHour = new int[24];
        parkingRecordRepository.findByEntryAtBetween(start, end)
                .forEach(record -> entryByHour[record.getEntryAt().getHour()]++);

        int[] exitByHour = new int[24];
        parkingRecordRepository.findByExitAtBetween(start, end)
                .forEach(record -> exitByHour[record.getExitAt().getHour()]++);

        List<TrafficResponse> traffic = new ArrayList<>(24);
        for (int hour = 0; hour < 24; hour++) {
            traffic.add(TrafficResponse.builder()
                    .hour(hour)
                    .entryCount(entryByHour[hour])
                    .exitCount(exitByHour[hour])
                    .build());
        }
        return traffic;
    }

    private DashboardEventResponse toEntryOccurrence(ParkingRecord record, Map<Long, String> gateNames) {
        return DashboardEventResponse.builder()
                .occurredAt(record.getEntryAt())
                .gateName(gateNames.get(record.getEntryGateId()))
                .vehicleNo(record.getVehicleNo())
                .entryType(resolveEntryType(record))
                .eventStatus(resolveEventStatus(record, false))
                .note(null)
                .build();
    }

    private DashboardEventResponse toExitOccurrence(ParkingRecord record, Map<Long, String> gateNames) {
        return DashboardEventResponse.builder()
                .occurredAt(record.getExitAt())
                .gateName(gateNames.get(record.getExitGateId()))
                .vehicleNo(record.getVehicleNo())
                .entryType(resolveEntryType(record))
                .eventStatus(resolveEventStatus(record, true))
                .note(null)
                .build();
    }

    private String resolveEntryType(ParkingRecord record) {
        return record.getSeasonPassId() != null ? "SEASON_PASS" : "GENERAL";
    }

    /**
     * eventStatus 산출 규칙은 design.md에 명시적 정의가 없어 상태값 4종(NORMAL/SETTLED/PASS/WARNING)의
     * 의미에 맞춰 최소 규칙으로 추정했다(가정 — Issues 참고).
     */
    private String resolveEventStatus(ParkingRecord record, boolean isExitOccurrence) {
        if (record.getVehicleNo() == null) {
            return "WARNING";
        }
        if ("SEASON_PASS".equals(resolveEntryType(record))) {
            return "PASS";
        }
        if (isExitOccurrence && "SETTLED".equals(record.getSettlementStatus())) {
            return "SETTLED";
        }
        return "NORMAL";
    }
}
