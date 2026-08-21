package com.parking.admin.service;

import com.parking.admin.dto.PlateCorrectionRequest;
import com.parking.admin.dto.PlateCorrectionResponse;

/**
 * 번호판·종별·입차일시 보정, 보정 후 상태 전이(UNRECOGNIZED -> NORMAL).
 * 근거: design.md [Screen 2] 모듈경계 LprCorrectionService
 */
public interface LprCorrectionService {

    PlateCorrectionResponse correctPlate(Long recordId, PlateCorrectionRequest request);
}
