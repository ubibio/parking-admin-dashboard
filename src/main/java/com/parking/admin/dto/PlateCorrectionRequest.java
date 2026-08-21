package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * PUT /api/parking-records/{recordId}/plate-correction 요청 바디.
 * reasonCode/reasonText 미입력 시 서비스 계층에서 400 처리(사유 필수).
 * 근거: design.md [Screen 2] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlateCorrectionRequest {

    private String vehicleNo;

    /** GENERAL / COMPACT / EV */
    private String vehicleType;

    private LocalDateTime entryAt;

    private String reasonCode;

    private String reasonText;
}
