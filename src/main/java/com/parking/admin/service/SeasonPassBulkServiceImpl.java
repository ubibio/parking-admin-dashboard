package com.parking.admin.service;

import com.parking.admin.dto.SeasonPassBulkUploadError;
import com.parking.admin.dto.SeasonPassBulkUploadResponse;
import com.parking.admin.dto.SeasonPassCreateResponse;
import com.parking.admin.dto.SeasonPassSaveRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassBulkService, 입출력계약 GET excel-template / POST bulk-upload,
 * [Do NOT](한 행 실패로 전체 롤백 금지).
 * 행 단위 저장은 SeasonPassService.create()(자체 @Transactional)를 그대로 재사용해 행마다 독립 커밋되도록 한다
 * — 이 클래스의 bulkUpload()에는 의도적으로 @Transactional을 걸지 않는다(가정 — Issues 참고).
 * CSV 업로드는 이 태스크 범위에서 다루지 않는다(brief에 POI만 명시 — Issues 참고).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonPassBulkServiceImpl implements SeasonPassBulkService {

    private static final String[] HEADERS = {
            "차량번호", "차주명", "연락처", "정기권종류", "유효시작일", "유효만료일", "결제상태", "허용게이트ID(콤마구분,비우면전체허용)"
    };
    private static final String[] EXAMPLE_ROW = {
            "12가3456", "홍길동", "010-1234-5678", "MONTHLY", "2026-01-01", "2026-01-31", "PAID", ""
    };
    private static final String[][] CODE_GUIDE_ROWS = {
            {"passType", "MONTHLY", "월정기권"},
            {"passType", "SEMI_ANNUAL", "정육정기권"},
            {"passType", "RESIDENT_EMPLOYEE", "입주민/임직원"},
            {"passType", "DAY", "주간 전용"},
            {"passType", "NIGHT", "야간 전용"},
            {"paymentStatus", "PAID", "결제완료"},
            {"paymentStatus", "FREE_APPROVED", "무료승인"}
    };
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int HEADER_ROW_INDEX = 0;
    private static final int FIRST_DATA_ROW_INDEX = 1;

    private final SeasonPassService seasonPassService;

    @Override
    public byte[] generateTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet dataSheet = workbook.createSheet("정기권등록");
            writeRow(dataSheet, 0, HEADERS);
            writeRow(dataSheet, 1, EXAMPLE_ROW);

            Sheet guideSheet = workbook.createSheet("코드안내");
            writeRow(guideSheet, 0, new String[] {"필드", "코드값", "설명"});
            for (int i = 0; i < CODE_GUIDE_ROWS.length; i++) {
                writeRow(guideSheet, i + 1, CODE_GUIDE_ROWS[i]);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("[SeasonPassBulkServiceImpl :: generateTemplate] :: 엑셀 양식 생성 실패 => {}", e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public SeasonPassBulkUploadResponse bulkUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드 파일이 비어 있습니다.");
        }

        int totalRows = 0;
        int successCount = 0;
        List<SeasonPassBulkUploadError> errors = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int rowIndex = FIRST_DATA_ROW_INDEX; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isRowEmpty(row, dataFormatter)) {
                    continue;
                }
                int rowNo = rowIndex - FIRST_DATA_ROW_INDEX + 1;
                totalRows++;
                String vehicleNo = cellText(row, 0, dataFormatter);
                try {
                    SeasonPassSaveRequest request = parseRow(row, dataFormatter);
                    SeasonPassCreateResponse created = seasonPassService.create(request);
                    log.info("[SeasonPassBulkServiceImpl :: bulkUpload] :: 행 등록 성공 => rowNo={}, passId={}",
                            rowNo, created.getPassId());
                    successCount++;
                } catch (ResponseStatusException e) {
                    errors.add(SeasonPassBulkUploadError.builder().rowNo(rowNo).vehicleNo(vehicleNo).message(e.getReason()).build());
                } catch (RuntimeException e) {
                    errors.add(SeasonPassBulkUploadError.builder().rowNo(rowNo).vehicleNo(vehicleNo).message(e.getMessage()).build());
                }
            }
        } catch (IOException e) {
            log.error("[SeasonPassBulkServiceImpl :: bulkUpload] :: 파일 파싱 실패 => {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "엑셀 파일을 읽을 수 없습니다.", e);
        }

        return SeasonPassBulkUploadResponse.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .failCount(errors.size())
                .errors(errors)
                .build();
    }

    private SeasonPassSaveRequest parseRow(Row row, DataFormatter dataFormatter) {
        String vehicleNo = requireText(row, 0, dataFormatter, "차량번호");
        String ownerName = requireText(row, 1, dataFormatter, "차주명");
        String phone = requireText(row, 2, dataFormatter, "연락처");
        String passType = requireText(row, 3, dataFormatter, "정기권종류");
        LocalDate validFrom = requireDate(row, 4, dataFormatter, "유효시작일");
        LocalDate validTo = requireDate(row, 5, dataFormatter, "유효만료일");
        String paymentStatus = requireText(row, 6, dataFormatter, "결제상태");
        List<Long> allowedGateIds = parseGateIds(cellText(row, 7, dataFormatter));

        return SeasonPassSaveRequest.builder()
                .vehicleNo(vehicleNo)
                .ownerName(ownerName)
                .phone(phone)
                .passType(passType)
                .validFrom(validFrom)
                .validTo(validTo)
                .paymentStatus(paymentStatus)
                .allowedGateIds(allowedGateIds)
                .build();
    }

    private List<Long> parseGateIds(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return Arrays.stream(rawValue.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .toList();
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "허용게이트ID 형식이 올바르지 않습니다: " + rawValue);
        }
    }

    private String requireText(Row row, int colIndex, DataFormatter dataFormatter, String fieldLabel) {
        String value = cellText(row, colIndex, dataFormatter);
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldLabel + "은(는) 필수입니다.");
        }
        return value;
    }

    private LocalDate requireDate(Row row, int colIndex, DataFormatter dataFormatter, String fieldLabel) {
        Cell cell = row.getCell(colIndex);
        if (cell != null && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String text = requireText(row, colIndex, dataFormatter, fieldLabel);
        try {
            return LocalDate.parse(text, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    fieldLabel + " 형식이 올바르지 않습니다(yyyy-MM-dd): " + text);
        }
    }

    private String cellText(Row row, int colIndex, DataFormatter dataFormatter) {
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }
        String text = dataFormatter.formatCellValue(cell).trim();
        return text.isEmpty() ? null : text;
    }

    private boolean isRowEmpty(Row row, DataFormatter dataFormatter) {
        if (row == null) {
            return true;
        }
        for (int col = 0; col < HEADERS.length; col++) {
            if (cellText(row, col, dataFormatter) != null) {
                return false;
            }
        }
        return true;
    }

    private void writeRow(Sheet sheet, int rowIndex, String[] values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }
}
