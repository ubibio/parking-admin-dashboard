package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * GET /api/season-passes 목록 항목. ownerName/phone은 마스킹된 값(SeasonPassSearchServiceImpl에서 변환).
 * 근거: design.md [Screen 4] 입출력계약, 비기능요구사항 §2 마스킹
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassListItemResponse {

    private Long passId;
    private String vehicleNo;

    /** 마스킹된 값(예: 김*수) */
    private String ownerName;

    /** 마스킹된 값(예: 010-****-1234) */
    private String phone;

    /** MONTHLY / SEMI_ANNUAL / RESIDENT_EMPLOYEE / DAY / NIGHT */
    private String passType;

    private LocalDate validFrom;
    private LocalDate validTo;

    /** PAID / FREE_APPROVED */
    private String paymentStatus;

    /** ACTIVE / EXPIRING_SOON / EXPIRED */
    private String passStatus;
}
