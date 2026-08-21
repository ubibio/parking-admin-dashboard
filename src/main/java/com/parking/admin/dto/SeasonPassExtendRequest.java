package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * POST /api/season-passes/extend 요청 바디. extendMonths와 newValidTo 중 하나를 사용(newValidTo 우선).
 * 근거: design.md [Screen 4] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassExtendRequest {

    private List<Long> passIds;

    /** 기존 유효 만료일에 개월 수를 더함 */
    private Integer extendMonths;

    /** 지정 시 extendMonths보다 우선 적용 */
    private LocalDate newValidTo;
}
