package com.parking.admin.service;

import com.parking.admin.dto.SeasonPassDetailResponse;
import com.parking.admin.dto.SeasonPassListResponse;
import com.parking.admin.dto.SeasonPassSearchCondition;

/**
 * 키워드(차량번호/차주명/연락처)·상태·구분 검색 + 페이징, 마스킹 DTO 변환.
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassSearchService
 */
public interface SeasonPassSearchService {

    SeasonPassListResponse search(SeasonPassSearchCondition condition, int page, int size);

    /** 수정 폼 초기값 채우기 전용 — ownerName/phone 마스킹 없이 원본 반환. 근거: 6-4 Issue#1 보완 */
    SeasonPassDetailResponse getDetail(Long passId);
}
