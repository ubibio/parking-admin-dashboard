package com.parking.admin.controller;

import com.parking.admin.dto.SeasonPassBulkUploadResponse;
import com.parking.admin.service.SeasonPassBulkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 정기권 등록 양식(.xlsx) 다운로드 / 엑셀 대량 업로드(행 단위 부분 성공).
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassBulkApiController, 입출력계약 excel-template / bulk-upload
 */
@RestController
@RequestMapping("/api/season-passes")
@RequiredArgsConstructor
public class SeasonPassBulkApiController {

    private final SeasonPassBulkService seasonPassBulkService;

    @GetMapping("/excel-template")
    public ResponseEntity<byte[]> excelTemplate() {
        byte[] excelBytes = seasonPassBulkService.generateTemplate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"season-pass-template.xlsx\"")
                .body(excelBytes);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SeasonPassBulkUploadResponse bulkUpload(@RequestParam("file") MultipartFile file) {
        return seasonPassBulkService.bulkUpload(file);
    }
}
