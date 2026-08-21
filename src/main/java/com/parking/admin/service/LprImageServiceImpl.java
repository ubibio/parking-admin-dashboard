package com.parking.admin.service;

import com.parking.admin.dto.ParkingRecordImages;
import com.parking.admin.entity.ParkingRecord;
import org.springframework.stereotype.Service;

/**
 * 근거: design.md [Screen 2] 모듈경계 LprImageService, [Do NOT] "원본 스토리지 URL을 응답에 그대로 담지 말 것"
 * 가정(Issues 참고): 이미지 저장소·실제 바이트 서빙 엔드포인트는 이 브리프 범위 밖이라 구현하지 않는다.
 * 여기서는 recordId 기반의 불투명 경로만 발급해 full_image_path/plate_crop_image_path 원본 값이
 * 절대 응답에 섞이지 않게 하는 최소 책임만 진다.
 */
@Service
public class LprImageServiceImpl implements LprImageService {

    private static final String IMAGE_URL_PREFIX = "/api/parking-records/";

    @Override
    public ParkingRecordImages buildImages(ParkingRecord record) {
        return ParkingRecordImages.builder()
                .fullImageUrl(record.getFullImagePath() != null ? buildUrl(record.getRecordId(), "full") : null)
                .plateCropImageUrl(record.getPlateCropImagePath() != null ? buildUrl(record.getRecordId(), "plate-crop") : null)
                .build();
    }

    private String buildUrl(Long recordId, String type) {
        return IMAGE_URL_PREFIX + recordId + "/images/" + type;
    }
}
