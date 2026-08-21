package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * POST /api/season-passes/extend, DELETE /api/season-passes 공용 응답(부분 성공).
 * 근거: design.md [Screen 4] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassBulkActionResponse {

    private int successCount;
    private List<SeasonPassBulkActionFailure> failed;
}
