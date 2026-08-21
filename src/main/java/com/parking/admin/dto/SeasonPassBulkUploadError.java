package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * POST /api/season-passes/bulk-upload 응답 errors[] 항목.
 * 근거: design.md [Screen 4] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassBulkUploadError {

    private int rowNo;
    private String vehicleNo;
    private String message;
}
