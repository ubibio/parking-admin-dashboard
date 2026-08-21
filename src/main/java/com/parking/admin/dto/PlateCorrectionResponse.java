package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * PUT /api/parking-records/{recordId}/plate-correction 응답.
 * 근거: design.md [Screen 2] 입출력계약
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlateCorrectionResponse {

    private Long recordId;

    /** 미인식 건 보정 저장 시 NORMAL로 전이(backend-schema.md §3 상태 전이) */
    private String recordStatus;

    private LocalDateTime updatedAt;
}
