package com.parking.admin.service;

import com.parking.admin.dto.RevenueDetailItemResponse;
import com.parking.admin.entity.ParkingRecord;
import com.parking.admin.repository.ParkingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 근거: design.md [Screen 3] 모듈경계 RevenueExcelService, 입출력계약 GET /api/revenue/excel?target=DETAIL|RAW.
 * DETAIL = RevenueStatisticsService.getDetails() 결과(일자/월/년별 상세) 그대로 시트화.
 * RAW = 원천 PARKING_RECORD 행(정산 확정 시점인 exitAt 기준, fromDate~toDate) 그대로 시트화(가정 — Issues 참고).
 * SXSSFWorkbook(스트리밍)으로 대용량 다운로드 시 메모리 사용을 제한한다(ParkingRecordExcelServiceImpl과 동일 관례).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueExcelServiceImpl implements RevenueExcelService {

    private static final String TARGET_DETAIL = "DETAIL";
    private static final String TARGET_RAW = "RAW";

    private static final String[] DETAIL_HEADERS = {
            "구분(일자/월/년)", "입차수", "출차수", "정산건수", "기본요금", "할인금액", "카드결제", "간편결제"
    };
    private static final String[] RAW_HEADERS = {
            "recordId", "차량번호", "입차일시", "출차일시", "요금", "할인금액", "결제수단", "간편결제사", "정산상태"
    };
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RevenueStatisticsService revenueStatisticsService;
    private final ParkingRecordRepository parkingRecordRepository;

    @Override
    public byte[] generate(String periodType, LocalDate fromDate, LocalDate toDate, String target) {
        String resolvedTarget = requireTarget(target);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            if (TARGET_DETAIL.equals(resolvedTarget)) {
                writeDetailSheet(workbook, periodType, fromDate, toDate);
            } else {
                writeRawSheet(workbook, fromDate, toDate);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        } catch (IOException e) {
            log.error("[RevenueExcelServiceImpl :: generate] :: 엑셀 생성 실패 => {}", e.getMessage());
            throw new UncheckedIOException(e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 생성 중 오류가 발생했습니다.", e);
        }
    }

    private void writeDetailSheet(SXSSFWorkbook workbook, String periodType, LocalDate fromDate, LocalDate toDate) {
        List<RevenueDetailItemResponse> items = revenueStatisticsService.getDetails(periodType, fromDate, toDate).getItems();

        Sheet sheet = workbook.createSheet("매출상세");
        Row header = sheet.createRow(0);
        for (int i = 0; i < DETAIL_HEADERS.length; i++) {
            header.createCell(i).setCellValue(DETAIL_HEADERS[i]);
        }

        int rowIndex = 1;
        for (RevenueDetailItemResponse item : items) {
            Row row = sheet.createRow(rowIndex++);
            int col = 0;
            row.createCell(col++).setCellValue(item.getPeriodKey());
            row.createCell(col++).setCellValue(item.getEntryCount());
            row.createCell(col++).setCellValue(item.getExitCount());
            row.createCell(col++).setCellValue(item.getSettlementCount());
            row.createCell(col++).setCellValue(item.getBaseFeeAmount().doubleValue());
            row.createCell(col++).setCellValue(item.getDiscountAmount().doubleValue());
            row.createCell(col++).setCellValue(item.getCardAmount().doubleValue());
            row.createCell(col).setCellValue(item.getEasyPayAmount().doubleValue());
        }
    }

    private void writeRawSheet(SXSSFWorkbook workbook, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime start = requireStart(fromDate);
        LocalDateTime end = requireEnd(toDate);
        List<ParkingRecord> records = parkingRecordRepository.findByExitAtBetween(start, end);

        Sheet sheet = workbook.createSheet("원천데이터");
        Row header = sheet.createRow(0);
        for (int i = 0; i < RAW_HEADERS.length; i++) {
            header.createCell(i).setCellValue(RAW_HEADERS[i]);
        }

        int rowIndex = 1;
        for (ParkingRecord record : records) {
            Row row = sheet.createRow(rowIndex++);
            int col = 0;
            row.createCell(col++).setCellValue(record.getRecordId());
            row.createCell(col++).setCellValue(record.getVehicleNo() != null ? record.getVehicleNo() : "미인식");
            row.createCell(col++).setCellValue(formatDateTime(record.getEntryAt()));
            setCellOrBlank(row.createCell(col++), formatDateTime(record.getExitAt()));
            setNumericOrBlank(row.createCell(col++), record.getFeeAmount());
            setNumericOrBlank(row.createCell(col++), record.getDiscountAmount());
            setCellOrBlank(row.createCell(col++), record.getPaymentMethod());
            setCellOrBlank(row.createCell(col++), record.getEasyPayProvider());
            row.createCell(col).setCellValue(record.getSettlementStatus());
        }
    }

    private void setCellOrBlank(Cell cell, String value) {
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private void setNumericOrBlank(Cell cell, java.math.BigDecimal value) {
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
    }

    private String formatDateTime(LocalDateTime value) {
        return value != null ? value.format(DATE_TIME_FORMATTER) : null;
    }

    private String requireTarget(String target) {
        if (!TARGET_DETAIL.equals(target) && !TARGET_RAW.equals(target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "target은 DETAIL/RAW 중 하나여야 합니다.");
        }
        return target;
    }

    private LocalDateTime requireStart(LocalDate fromDate) {
        if (fromDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate는 필수입니다.");
        }
        return fromDate.atStartOfDay();
    }

    private LocalDateTime requireEnd(LocalDate toDate) {
        if (toDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toDate는 필수입니다.");
        }
        return toDate.plusDays(1).atStartOfDay();
    }
}
