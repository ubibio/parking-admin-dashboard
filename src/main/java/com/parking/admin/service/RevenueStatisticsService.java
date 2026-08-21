package com.parking.admin.service;

import com.parking.admin.dto.RevenueDetailByPeriodResponse;
import com.parking.admin.dto.RevenueDetailsResponse;
import com.parking.admin.dto.RevenueSummaryResponse;

import java.time.LocalDate;

/**
 * 기간 집계(요약 카드 + 일자별 상세) 및 결제수단 분류 집계.
 * periodType(DAILY/MONTHLY/YEARLY)에 따른 GROUP BY 키 산출은 이 서비스(구현체) 한 곳에만 둔다.
 * 근거: design.md [Screen 3] 모듈경계 RevenueStatisticsService
 */
public interface RevenueStatisticsService {

    RevenueSummaryResponse getSummary(LocalDate fromDate, LocalDate toDate);

    RevenueDetailsResponse getDetails(String periodType, LocalDate fromDate, LocalDate toDate);

    RevenueDetailByPeriodResponse getDetailByPeriod(String periodKey);
}
