package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * GET /api/season-passes 응답 래퍼.
 * 근거: design.md [Screen 4] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassListResponse {

    private long totalCount;
    private int page;
    private int size;
    private List<SeasonPassListItemResponse> items;
}
