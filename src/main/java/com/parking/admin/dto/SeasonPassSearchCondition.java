package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GET /api/season-passes 검색 조건. 키워드는 차량번호/차주명/연락처 대상(design.md [Screen 4] [완료조건]).
 * 근거: backend-schema.md §5 API
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassSearchCondition {

    /** 차량번호/차주명/연락처 부분 일치 */
    private String keyword;

    /** ACTIVE / EXPIRING_SOON / EXPIRED */
    private String status;

    /** MONTHLY / SEMI_ANNUAL / RESIDENT_EMPLOYEE / DAY / NIGHT */
    private String passType;
}
