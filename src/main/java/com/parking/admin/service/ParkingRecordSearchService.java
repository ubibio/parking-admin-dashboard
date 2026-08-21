package com.parking.admin.service;

import com.parking.admin.dto.ParkingRecordDetailResponse;
import com.parking.admin.dto.ParkingRecordListResponse;
import com.parking.admin.dto.ParkingRecordSearchCondition;
import com.parking.admin.entity.ParkingRecord;

import java.util.List;

/**
 * 다중 조건 검색 + 페이징(목록 JSON), 조건 재사용 전건 조회(엑셀), 상세 조회.
 * 근거: design.md [Screen 2] 모듈경계
 */
public interface ParkingRecordSearchService {

    ParkingRecordListResponse search(ParkingRecordSearchCondition condition, int page, int size);

    /** 엑셀 다운로드 전용 — 동일 조건, 페이징 없이 전건(design.md [Do NOT] 예외 허용 대상) */
    List<ParkingRecord> searchAll(ParkingRecordSearchCondition condition);

    ParkingRecordDetailResponse getDetail(Long recordId);
}
