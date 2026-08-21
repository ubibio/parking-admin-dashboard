package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GET /api/dashboard/traffic 응답 항목(시간대 1건).
 * 근거: design.md [Screen 1] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficResponse {

    /** 0~23 */
    private int hour;
    private int entryCount;
    private int exitCount;
}
