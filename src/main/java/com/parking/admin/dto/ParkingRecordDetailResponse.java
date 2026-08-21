package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GET /api/parking-records/{recordId} 응답. 예외 처리 모달 오픈 시 1회 호출.
 * 근거: design.md [Screen 2] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingRecordDetailResponse {

    private Long recordId;

    /** ENTRY / EXIT */
    private String direction;

    private String vehicleNo;

    /** GENERAL / COMPACT / EV */
    private String vehicleType;

    private LocalDateTime entryAt;

    private LocalDateTime exitAt;

    private Long parkingMinutes;

    private BigDecimal feeAmount;

    private BigDecimal discountAmount;

    /** NORMAL / UNRECOGNIZED / SEASON_PASS / MANUAL_EXIT */
    private String recordStatus;

    /** UNSETTLED / SETTLED / ADJUSTED */
    private String settlementStatus;

    private ParkingRecordImages images;
}
