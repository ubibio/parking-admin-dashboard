package com.parking.admin.service;

import com.parking.admin.dto.DashboardEventResponse;
import com.parking.admin.dto.TrafficResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * 최근 입출차 이벤트 조회, 시간대별 입/출차 집계.
 * PARKING_RECORD의 entry_at/exit_at 각각을 별도 발생(occurrence)으로 병합해 이벤트 피드를 구성한다.
 * 근거: design.md [Screen 1] 모듈경계, backend-schema.md §2 비고
 */
public interface ParkingEventLogService {

    List<DashboardEventResponse> getRecentEvents(int limit);

    List<TrafficResponse> getTraffic(LocalDate date);
}
