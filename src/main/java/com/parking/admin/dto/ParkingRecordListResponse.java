package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * GET /api/parking-records 응답 래퍼.
 * 근거: design.md [Screen 2] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingRecordListResponse {

    private long totalCount;
    private int page;
    private int size;
    private List<ParkingRecordListItemResponse> items;
}
