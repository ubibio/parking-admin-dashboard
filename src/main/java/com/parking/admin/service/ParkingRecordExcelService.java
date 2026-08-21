package com.parking.admin.service;

import com.parking.admin.dto.ParkingRecordSearchCondition;

/**
 * 조회 조건을 재사용해 .xlsx 스트리밍 생성.
 * 근거: design.md [Screen 2] 모듈경계 ParkingRecordExcelService
 */
public interface ParkingRecordExcelService {

    byte[] generate(ParkingRecordSearchCondition condition);
}
