package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * POST /api/season-passes, PUT /api/season-passes/{passId} 공용 요청 바디(동일 바디 — design.md [Screen 4] 입출력계약).
 * 근거: backend-schema.md §5 API
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassSaveRequest {

    private String vehicleNo;
    private String ownerName;
    private String phone;

    /** MONTHLY / SEMI_ANNUAL / RESIDENT_EMPLOYEE / DAY / NIGHT */
    private String passType;

    private LocalDate validFrom;
    private LocalDate validTo;

    /** 비어 있으면 전체 게이트 허용 */
    private List<Long> allowedGateIds;

    /** PAID / FREE_APPROVED */
    private String paymentStatus;
}
