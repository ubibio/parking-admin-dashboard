package com.parking.admin.service;

import com.parking.admin.dto.DashboardKpiResponse;

/**
 * 주차면수·재차대수·금일 입출차·금일 매출 집계 및 occupancyLevel 판정.
 * 근거: design.md [Screen 1] 모듈경계
 */
public interface DashboardKpiService {

    DashboardKpiResponse getKpi();
}
