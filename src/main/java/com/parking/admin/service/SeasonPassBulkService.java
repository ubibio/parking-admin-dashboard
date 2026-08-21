package com.parking.admin.service;

import com.parking.admin.dto.SeasonPassBulkUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 엑셀 양식 생성, 업로드 파싱·행 단위 검증·부분 성공 리포트.
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassBulkService
 */
public interface SeasonPassBulkService {

    byte[] generateTemplate();

    SeasonPassBulkUploadResponse bulkUpload(MultipartFile file);
}
