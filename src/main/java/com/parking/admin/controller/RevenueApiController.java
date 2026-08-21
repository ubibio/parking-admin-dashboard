package com.parking.admin.controller;

import com.parking.admin.dto.RevenueDetailByPeriodResponse;
import com.parking.admin.dto.RevenueDetailsResponse;
import com.parking.admin.dto.RevenueSummaryResponse;
import com.parking.admin.dto.StoreSettlementResponse;
import com.parking.admin.service.RevenueExcelService;
import com.parking.admin.service.RevenueStatisticsService;
import com.parking.admin.service.StoreDiscountSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 요약·상세·상가정산 조회, 엑셀 다운로드(읽기 전용 — 쓰기 API 없음).
 * STORE_OWNER의 store-settlement storeId 강제 치환은 StoreDiscountSettlementService에서 처리한다.
 * 근거: design.md [Screen 3] 모듈경계, 입출력계약, [Do NOT](결제취소/환불 실행 API 금지)
 */
@RestController
@RequestMapping("/api/revenue")
@RequiredArgsConstructor
public class RevenueApiController {

    private final RevenueStatisticsService revenueStatisticsService;
    private final StoreDiscountSettlementService storeDiscountSettlementService;
    private final RevenueExcelService revenueExcelService;

    @GetMapping("/summary")
    public RevenueSummaryResponse summary(
            @RequestParam(required = false) String periodType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        // periodType은 계약상 파라미터로 받되(design.md 입출력계약) 요약 카드는 기간 전체 합산값이라 그룹핑에 사용하지 않는다(가정 — Issues 참고).
        return revenueStatisticsService.getSummary(fromDate, toDate);
    }

    @GetMapping("/details")
    public RevenueDetailsResponse details(
            @RequestParam String periodType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return revenueStatisticsService.getDetails(periodType, fromDate, toDate);
    }

    @GetMapping("/details/{periodKey}")
    public RevenueDetailByPeriodResponse detailByPeriod(@PathVariable String periodKey) {
        return revenueStatisticsService.getDetailByPeriod(periodKey);
    }

    @GetMapping("/store-settlement")
    public StoreSettlementResponse storeSettlement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long storeId) {
        return storeDiscountSettlementService.getStoreSettlement(fromDate, toDate, storeId);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) String periodType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam String target) {
        byte[] excelBytes = revenueExcelService.generate(periodType, fromDate, toDate, target);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"revenue.xlsx\"")
                .body(excelBytes);
    }
}
