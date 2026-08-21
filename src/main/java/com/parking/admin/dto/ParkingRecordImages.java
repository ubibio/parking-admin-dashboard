package com.parking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상세 응답의 images 필드 — 토큰 URL만 담고 원본 저장 경로는 절대 담지 않는다.
 * 근거: design.md [Screen 2] 입출력계약, [Do NOT] "LPR 이미지의 파일시스템 경로나 원본 스토리지 URL을 응답에 그대로 담지 말 것"
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingRecordImages {

    /** 원본 경로가 없으면 null */
    private String fullImageUrl;

    /** 원본 경로가 없으면 null */
    private String plateCropImageUrl;
}
