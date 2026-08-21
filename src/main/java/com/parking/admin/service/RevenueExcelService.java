package com.parking.admin.service;

import java.time.LocalDate;

/**
 * 상세 내역(target=DETAIL)·원천 데이터(target=RAW) .xlsx 생성.
 * 근거: design.md [Screen 3] 모듈경계 RevenueExcelService, 입출력계약 GET /api/revenue/excel
 */
public interface RevenueExcelService {

    byte[] generate(String periodType, LocalDate fromDate, LocalDate toDate, String target);
}
