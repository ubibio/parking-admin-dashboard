package com.parking.admin.service;

import com.parking.admin.dto.ParkingRecordImages;
import com.parking.admin.entity.ParkingRecord;

/**
 * ParkingRecord의 내부 저장 경로(full_image_path/plate_crop_image_path)를 응답에 직접 노출하지 않고
 * 토큰 URL로 변환한다.
 * 근거: design.md [Screen 2] 모듈경계 LprImageService, [Do NOT]
 */
public interface LprImageService {

    ParkingRecordImages buildImages(ParkingRecord record);
}
