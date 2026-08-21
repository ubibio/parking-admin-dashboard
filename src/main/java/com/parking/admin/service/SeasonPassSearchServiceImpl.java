package com.parking.admin.service;

import com.parking.admin.dto.SeasonPassDetailResponse;
import com.parking.admin.dto.SeasonPassListItemResponse;
import com.parking.admin.dto.SeasonPassListResponse;
import com.parking.admin.dto.SeasonPassSearchCondition;
import com.parking.admin.entity.SeasonPass;
import com.parking.admin.repository.SeasonPassAllowedGateRepository;
import com.parking.admin.repository.SeasonPassRepository;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassSearchService, backend-schema.md §5.
 * 목록 응답의 ownerName/phone은 반드시 MaskingSupport를 거쳐 마스킹된 값만 담는다(design.md [Do NOT]).
 */
@Service
@RequiredArgsConstructor
public class SeasonPassSearchServiceImpl implements SeasonPassSearchService {

    private final SeasonPassRepository seasonPassRepository;
    private final SeasonPassAllowedGateRepository seasonPassAllowedGateRepository;
    private final MaskingSupport maskingSupport;

    @Override
    public SeasonPassListResponse search(SeasonPassSearchCondition condition, int page, int size) {
        Specification<SeasonPass> spec = buildSpecification(condition);
        Page<SeasonPass> result = seasonPassRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "passId")));

        List<SeasonPassListItemResponse> items = result.getContent().stream()
                .map(this::toListItem)
                .toList();

        return SeasonPassListResponse.builder()
                .totalCount(result.getTotalElements())
                .page(page)
                .size(size)
                .items(items)
                .build();
    }

    @Override
    public SeasonPassDetailResponse getDetail(Long passId) {
        SeasonPass pass = seasonPassRepository.findById(passId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "정기권을 찾을 수 없습니다: " + passId));
        List<Long> allowedGateIds = seasonPassAllowedGateRepository.findByIdPassId(passId).stream()
                .map(gate -> gate.getId().getGateId())
                .toList();

        return SeasonPassDetailResponse.builder()
                .vehicleNo(pass.getVehicleNo())
                .ownerName(pass.getOwnerName())
                .phone(pass.getPhone())
                .passType(pass.getPassType())
                .validFrom(pass.getValidFrom())
                .validTo(pass.getValidTo())
                .allowedGateIds(allowedGateIds)
                .paymentStatus(pass.getPaymentStatus())
                .build();
    }

    private SeasonPassListItemResponse toListItem(SeasonPass pass) {
        return SeasonPassListItemResponse.builder()
                .passId(pass.getPassId())
                .vehicleNo(pass.getVehicleNo())
                .ownerName(maskingSupport.maskName(pass.getOwnerName()))
                .phone(maskingSupport.maskPhone(pass.getPhone()))
                .passType(pass.getPassType())
                .validFrom(pass.getValidFrom())
                .validTo(pass.getValidTo())
                .paymentStatus(pass.getPaymentStatus())
                .passStatus(pass.getPassStatus())
                .build();
    }

    /** 근거: backend-schema.md §5 API 검색 조건(keyword/status/passType) */
    private Specification<SeasonPass> buildSpecification(SeasonPassSearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (condition != null && StringUtils.hasText(condition.getKeyword())) {
                String keyword = "%" + condition.getKeyword().trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("vehicleNo"), keyword),
                        cb.like(root.get("ownerName"), keyword),
                        cb.like(root.get("phone"), keyword)
                ));
            }
            if (condition != null && StringUtils.hasText(condition.getStatus())) {
                predicates.add(cb.equal(root.get("passStatus"), condition.getStatus()));
            }
            if (condition != null && StringUtils.hasText(condition.getPassType())) {
                predicates.add(cb.equal(root.get("passType"), condition.getPassType()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
