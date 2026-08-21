package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * POST /api/season-passes/bulk-upload 응답. 행 단위 부분 성공(design.md [Do NOT] 전체 롤백 금지).
 * 근거: design.md [Screen 4] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassBulkUploadResponse {

    private int totalRows;
    private int successCount;
    private int failCount;
    private List<SeasonPassBulkUploadError> errors;
}
