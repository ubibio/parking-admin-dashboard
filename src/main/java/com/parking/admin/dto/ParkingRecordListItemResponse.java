package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GET /api/parking-records 목록 항목.
 * 근거: design.md [Screen 2] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingRecordListItemResponse {

    private Long recordId;

    /** ENTRY / EXIT — exitAt IS NULL 여부로 파생 */
    private String direction;

    /** 미인식 건은 null */
    private String vehicleNo;

    private LocalDateTime entryAt;

    private LocalDateTime exitAt;

    /** exitAt이 있을 때만 산출(진행 중 세션은 null, 가정 — Issues 참고) */
    private Long parkingMinutes;

    private BigDecimal feeAmount;

    private boolean hasImage;

    /** NORMAL / UNRECOGNIZED / SEASON_PASS / MANUAL_EXIT */
    private String recordStatus;
}
