package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * POST /api/season-passes/notifications 요청 바디. passIds[] 지정 시 대상 고정, 미지정 시 dDay(7|3) 기준 대상 산출.
 * 근거: design.md [Screen 4] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassNotificationRequest {

    private List<Long> passIds;

    /** 7 또는 3 */
    private Integer dDay;

    /** ALIM_TALK / SMS */
    private String channel;
}
