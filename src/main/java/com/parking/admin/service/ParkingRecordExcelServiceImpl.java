package com.parking.admin.service;

import com.parking.admin.dto.ParkingRecordSearchCondition;
import com.parking.admin.entity.ParkingRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 근거: design.md [Screen 2] 모듈경계 ParkingRecordExcelService, 입출력계약 GET /api/parking-records/excel
 * "관리"(수정/강제출차 버튼) 컬럼은 화면 조작 전용이라 엑셀에 포함하지 않는다(가정 — Issues 참고).
 * SXSSFWorkbook(스트리밍)으로 대용량 다운로드 시 메모리 사용을 제한한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingRecordExcelServiceImpl implements ParkingRecordExcelService {

    private static final String[] HEADERS = {
            "No", "구분", "차량번호", "입차일시", "출차일시", "주차시간(분)", "요금", "이미지유무", "상태"
    };
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ParkingRecordSearchService parkingRecordSearchService;

    @Override
    public byte[] generate(ParkingRecordSearchCondition condition) {
        List<ParkingRecord> records = parkingRecordSearchService.searchAll(condition);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("입출차내역");
            writeHeader(sheet);
            writeRows(sheet, records);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        } catch (IOException e) {
            log.error("[ParkingRecordExcelServiceImpl :: generate] :: 엑셀 생성 실패 => {}", e.getMessage());
            throw new UncheckedIOException(e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 생성 중 오류가 발생했습니다.", e);
        }
    }

    private void writeHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            header.createCell(i).setCellValue(HEADERS[i]);
        }
    }

    private void writeRows(Sheet sheet, List<ParkingRecord> records) {
        int rowIndex = 1;
        int no = 1;
        for (ParkingRecord record : records) {
            Row row = sheet.createRow(rowIndex++);
            int col = 0;
            row.createCell(col++).setCellValue(no++);
            row.createCell(col++).setCellValue(record.getExitAt() == null ? "입차" : "출차");
            row.createCell(col++).setCellValue(record.getVehicleNo() != null ? record.getVehicleNo() : "미인식");
            row.createCell(col++).setCellValue(formatDateTime(record.getEntryAt()));
            setCellOrBlank(row.createCell(col++), formatDateTime(record.getExitAt()));
            Long parkingMinutes = resolveParkingMinutes(record);
            if (parkingMinutes != null) {
                row.createCell(col++).setCellValue(parkingMinutes);
            } else {
                col++;
            }
            if (record.getFeeAmount() != null) {
                row.createCell(col++).setCellValue(record.getFeeAmount().doubleValue());
            } else {
                col++;
            }
            row.createCell(col++).setCellValue(record.getFullImagePath() != null ? "Y" : "N");
            row.createCell(col).setCellValue(record.getRecordStatus());
        }
    }

    private void setCellOrBlank(Cell cell, String value) {
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private Long resolveParkingMinutes(ParkingRecord record) {
        if (record.getExitAt() == null) {
            return null;
        }
        return Duration.between(record.getEntryAt(), record.getExitAt()).toMinutes();
    }

    private String formatDateTime(java.time.LocalDateTime value) {
        return value != null ? value.format(DATE_TIME_FORMATTER) : null;
    }
}
