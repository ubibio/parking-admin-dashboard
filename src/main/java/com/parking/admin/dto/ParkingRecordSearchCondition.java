package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * GET /api/parking-records, GET /api/parking-records/excel 공유 검색 조건.
 * 목록 JSON과 엑셀이 동일 인스턴스를 사용해 조건 불일치를 구조적으로 차단한다(design.md [Screen 2] 모듈경계).
 * page/size는 목록 조회 전용 페이징 파라미터라 이 DTO에 포함하지 않는다(엑셀은 전건).
 * 근거: backend-schema.md §3 API GET /api/parking-records
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingRecordSearchCondition {

    /** entryAt 기준 조회 시작일(포함) — 기간 필터를 entryAt에 적용(가정, Issues 참고) */
    private LocalDate fromDate;

    /** entryAt 기준 조회 종료일(포함) */
    private LocalDate toDate;

    /** ENTRY / EXIT */
    private String direction;

    /** NORMAL / UNRECOGNIZED / SEASON_PASS / MANUAL_EXIT */
    private String status;

    /** 전체 번호(부분 일치) 또는 숫자 4자리인 경우 뒤 4자리(vehicleNoSuffix) 일치 */
    private String vehicleNo;
}
