package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * POST /api/season-passes/notifications 응답.
 * 근거: design.md [Screen 4] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassNotificationResponse {

    private int requestedCount;
    private int sentCount;
    private List<SeasonPassBulkActionFailure> failed;
}
