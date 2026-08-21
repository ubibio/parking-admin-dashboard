package com.parking.admin.service;

import com.parking.admin.dto.ParkingRecordDetailResponse;
import com.parking.admin.dto.ParkingRecordListItemResponse;
import com.parking.admin.dto.ParkingRecordListResponse;
import com.parking.admin.dto.ParkingRecordSearchCondition;
import com.parking.admin.entity.ParkingRecord;
import com.parking.admin.repository.ParkingRecordRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 근거: design.md [Screen 2] 모듈경계 ParkingRecordSearchService, backend-schema.md §3
 */
@Service
@RequiredArgsConstructor
public class ParkingRecordSearchServiceImpl implements ParkingRecordSearchService {

    private static final String DIRECTION_ENTRY = "ENTRY";
    private static final String DIRECTION_EXIT = "EXIT";
    private static final int VEHICLE_NO_SUFFIX_LENGTH = 4;

    private final ParkingRecordRepository parkingRecordRepository;
    private final LprImageService lprImageService;

    @Override
    public ParkingRecordListResponse search(ParkingRecordSearchCondition condition, int page, int size) {
        Specification<ParkingRecord> spec = buildSpecification(condition);
        Page<ParkingRecord> result = parkingRecordRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "entryAt")));

        List<ParkingRecordListItemResponse> items = result.getContent().stream()
                .map(this::toListItem)
                .toList();

        return ParkingRecordListResponse.builder()
                .totalCount(result.getTotalElements())
                .page(page)
                .size(size)
                .items(items)
                .build();
    }

    @Override
    public List<ParkingRecord> searchAll(ParkingRecordSearchCondition condition) {
        Specification<ParkingRecord> spec = buildSpecification(condition);
        return parkingRecordRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "entryAt"));
    }

    @Override
    public ParkingRecordDetailResponse getDetail(Long recordId) {
        ParkingRecord record = getRecordOrThrow(recordId);
        return ParkingRecordDetailResponse.builder()
                .recordId(record.getRecordId())
                .direction(resolveDirection(record))
                .vehicleNo(record.getVehicleNo())
                .vehicleType(record.getVehicleType())
                .entryAt(record.getEntryAt())
                .exitAt(record.getExitAt())
                .parkingMinutes(resolveParkingMinutes(record))
                .feeAmount(record.getFeeAmount())
                .discountAmount(record.getDiscountAmount())
                .recordStatus(record.getRecordStatus())
                .settlementStatus(record.getSettlementStatus())
                .images(lprImageService.buildImages(record))
                .build();
    }

    private ParkingRecord getRecordOrThrow(Long recordId) {
        return parkingRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "입출차 기록을 찾을 수 없습니다: " + recordId));
    }

    private ParkingRecordListItemResponse toListItem(ParkingRecord record) {
        return ParkingRecordListItemResponse.builder()
                .recordId(record.getRecordId())
                .direction(resolveDirection(record))
                .vehicleNo(record.getVehicleNo())
                .entryAt(record.getEntryAt())
                .exitAt(record.getExitAt())
                .parkingMinutes(resolveParkingMinutes(record))
                .feeAmount(record.getFeeAmount())
                .hasImage(record.getFullImagePath() != null)
                .recordStatus(record.getRecordStatus())
                .build();
    }

    private String resolveDirection(ParkingRecord record) {
        return record.getExitAt() == null ? DIRECTION_ENTRY : DIRECTION_EXIT;
    }

    /** exitAt이 없는(진행 중) 세션은 null 반환(가정 — Issues 참고) */
    private Long resolveParkingMinutes(ParkingRecord record) {
        if (record.getExitAt() == null) {
            return null;
        }
        return Duration.between(record.getEntryAt(), record.getExitAt()).toMinutes();
    }

    /** 근거: backend-schema.md §3 검색 조건. 기간 필터는 entryAt 기준(가정 — Issues 참고) */
    private Specification<ParkingRecord> buildSpecification(ParkingRecordSearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (condition.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("entryAt"), condition.getFromDate().atStartOfDay()));
            }
            if (condition.getToDate() != null) {
                predicates.add(cb.lessThan(root.get("entryAt"), condition.getToDate().plusDays(1).atStartOfDay()));
            }
            if (DIRECTION_ENTRY.equals(condition.getDirection())) {
                predicates.add(cb.isNull(root.get("exitAt")));
            } else if (DIRECTION_EXIT.equals(condition.getDirection())) {
                predicates.add(cb.isNotNull(root.get("exitAt")));
            }
            if (StringUtils.hasText(condition.getStatus())) {
                predicates.add(cb.equal(root.get("recordStatus"), condition.getStatus()));
            }
            if (StringUtils.hasText(condition.getVehicleNo())) {
                String vehicleNo = condition.getVehicleNo().trim();
                if (isSuffixSearch(vehicleNo)) {
                    predicates.add(cb.equal(root.get("vehicleNoSuffix"), vehicleNo));
                } else {
                    predicates.add(cb.like(root.get("vehicleNo"), "%" + vehicleNo + "%"));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** 숫자 4자리면 뒤 4자리 검색으로 판단(design.md [Screen 2] 입출력계약 비고) */
    private boolean isSuffixSearch(String vehicleNo) {
        return vehicleNo.length() == VEHICLE_NO_SUFFIX_LENGTH && vehicleNo.chars().allMatch(Character::isDigit);
    }
}
