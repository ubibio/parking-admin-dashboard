package com.parking.admin.service;

import com.parking.admin.dto.SeasonPassBulkActionResponse;
import com.parking.admin.dto.SeasonPassCreateResponse;
import com.parking.admin.dto.SeasonPassExtendRequest;
import com.parking.admin.dto.SeasonPassSaveRequest;
import com.parking.admin.dto.SeasonPassUpdateResponse;

import java.util.List;

/**
 * 단건 CRUD, 다중 연장·삭제, 게이트 제한 설정, 유효기간 검증.
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassService
 */
public interface SeasonPassService {

    SeasonPassCreateResponse create(SeasonPassSaveRequest request);

    SeasonPassUpdateResponse update(Long passId, SeasonPassSaveRequest request);

    SeasonPassBulkActionResponse extend(SeasonPassExtendRequest request);

    SeasonPassBulkActionResponse delete(List<Long> passIds);
}
