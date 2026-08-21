package com.parking.admin.service;

import com.parking.admin.dto.StoreSettlementResponse;

import java.time.LocalDate;

/**
 * 상가별 쿠폰 발행/사용 집계 및 청구용 보고서 데이터 산출.
 * STORE_OWNER 로그인 시 storeId 파라미터는 무시하고 세션 소속 storeId로 강제 치환한다(파라미터 신뢰 금지).
 * 근거: design.md [Screen 3] 모듈경계 StoreDiscountSettlementService, 완료조건
 */
public interface StoreDiscountSettlementService {

    StoreSettlementResponse getStoreSettlement(LocalDate fromDate, LocalDate toDate, Long storeId);
}
