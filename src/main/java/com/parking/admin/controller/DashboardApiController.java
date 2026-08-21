package com.parking.admin.controller;

import com.parking.admin.dto.DashboardEventResponse;
import com.parking.admin.dto.DashboardKpiResponse;
import com.parking.admin.dto.GateStatusResponse;
import com.parking.admin.dto.TrafficResponse;
import com.parking.admin.service.DashboardKpiService;
import com.parking.admin.service.GateStatusService;
import com.parking.admin.service.ParkingEventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * KPI·게이트 상태·이벤트 로그·트래픽 조회(읽기 전용 JSON).
 * 근거: design.md [Screen 1] 모듈경계, 입출력계약
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final DashboardKpiService dashboardKpiService;
    private final GateStatusService gateStatusService;
    private final ParkingEventLogService parkingEventLogService;

    @GetMapping("/kpi")
    public DashboardKpiResponse getKpi() {
        return dashboardKpiService.getKpi();
    }

    @GetMapping("/gates")
    public List<GateStatusResponse> getGates() {
        return gateStatusService.getGates();
    }

    @GetMapping("/events")
    public List<DashboardEventResponse> getEvents(@RequestParam(defaultValue = "20") int limit) {
        return parkingEventLogService.getRecentEvents(limit);
    }

    @GetMapping("/traffic")
    public List<TrafficResponse> getTraffic(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return parkingEventLogService.getTraffic(date);
    }
}
