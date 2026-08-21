package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * GET /api/season-passes/{passId} 응답. 수정 폼 초기값 채우기 전용 — ownerName/phone 마스킹 없이 원본 반환.
 * 필드는 SeasonPassSaveRequest와 대칭.
 * 근거: 6-4 Issue#1 보완(수정 폼 원본 값 조회 부재).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassDetailResponse {

    private String vehicleNo;

    /** 원본 값(마스킹 없음) */
    private String ownerName;

    /** 원본 값(마스킹 없음) */
    private String phone;

    /** MONTHLY / SEMI_ANNUAL / RESIDENT_EMPLOYEE / DAY / NIGHT */
    private String passType;

    private LocalDate validFrom;
    private LocalDate validTo;

    private List<Long> allowedGateIds;

    /** PAID / FREE_APPROVED */
    private String paymentStatus;
}
