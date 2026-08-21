package com.parking.admin.controller;

import com.parking.admin.dto.ForceExitRequest;
import com.parking.admin.dto.ForceExitResponse;
import com.parking.admin.dto.ParkingRecordDetailResponse;
import com.parking.admin.dto.ParkingRecordListResponse;
import com.parking.admin.dto.ParkingRecordSearchCondition;
import com.parking.admin.dto.PlateCorrectionRequest;
import com.parking.admin.dto.PlateCorrectionResponse;
import com.parking.admin.service.LprCorrectionService;
import com.parking.admin.service.ManualSettlementService;
import com.parking.admin.service.ParkingRecordExcelService;
import com.parking.admin.service.ParkingRecordSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 입/출차 목록·엑셀·상세 조회 + 번호판 보정(PUT)·강제 출차(POST).
 * brief Constraints "ParkingRecordApiController: 목록/엑셀다운로드/상세/보정/강제출차"에 따라 단일 컨트롤러로 구성
 * (design.md [Screen 2] 모듈경계는 쓰기 전용 ParkingRecordCorrectionApiController를 별도로 명시하나,
 *  이 작업 브리프 지시가 더 구체적이라 그대로 따랐다 — Issues 참고).
 * 보정·강제출차는 사유 필수 검증·AuditLog 기록을 서비스 계층에서 수행.
 * 근거: design.md [Screen 2] 모듈경계, 입출력계약, backend-schema.md §3 API
 */
@RestController
@RequestMapping("/api/parking-records")
@RequiredArgsConstructor
public class ParkingRecordApiController {

    private final ParkingRecordSearchService parkingRecordSearchService;
    private final ParkingRecordExcelService parkingRecordExcelService;
    private final LprCorrectionService lprCorrectionService;
    private final ManualSettlementService manualSettlementService;

    @GetMapping
    public ParkingRecordListResponse list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String vehicleNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return parkingRecordSearchService.search(buildCondition(fromDate, toDate, direction, status, vehicleNo), page, size);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String vehicleNo) {
        byte[] excelBytes = parkingRecordExcelService.generate(buildCondition(fromDate, toDate, direction, status, vehicleNo));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"parking-records.xlsx\"")
                .body(excelBytes);
    }

    @GetMapping("/{recordId}")
    public ParkingRecordDetailResponse detail(@PathVariable Long recordId) {
        return parkingRecordSearchService.getDetail(recordId);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SITE_OPERATOR')")
    @PutMapping("/{recordId}/plate-correction")
    public PlateCorrectionResponse plateCorrection(@PathVariable Long recordId, @RequestBody PlateCorrectionRequest request) {
        return lprCorrectionService.correctPlate(recordId, request);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{recordId}/force-exit")
    public ForceExitResponse forceExit(@PathVariable Long recordId, @RequestBody ForceExitRequest request) {
        return manualSettlementService.forceExit(recordId, request);
    }

    private ParkingRecordSearchCondition buildCondition(LocalDate fromDate, LocalDate toDate, String direction,
                                                          String status, String vehicleNo) {
        return ParkingRecordSearchCondition.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .direction(direction)
                .status(status)
                .vehicleNo(vehicleNo)
                .build();
    }
}
