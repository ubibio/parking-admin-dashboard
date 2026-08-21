package com.parking.admin.service;

import com.parking.admin.dto.StoreSettlementItemResponse;
import com.parking.admin.dto.StoreSettlementResponse;
import com.parking.admin.entity.AdminUser;
import com.parking.admin.entity.Store;
import com.parking.admin.repository.AdminUserRepository;
import com.parking.admin.repository.StoreDiscountCouponRepository;
import com.parking.admin.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 근거: design.md [Screen 3] 모듈경계 StoreDiscountSettlementService, 완료조건, [Do NOT](타 점포 정산 데이터 노출 금지).
 * STORE_OWNER 세션 소속 판별은 AuditLogServiceImpl의 actorId 산출과 동일하게
 * SecurityContextHolder → AdminUserRepository 조회 방식을 따른다(기존 관례 재사용).
 * billingAmount = 사용된 쿠폰 discountAmount 합계(가정 — Issues 참고).
 */
@Service
@RequiredArgsConstructor
public class StoreDiscountSettlementServiceImpl implements StoreDiscountSettlementService {

    private static final String ROLE_STORE_OWNER = "STORE_OWNER";
    private static final String ACTIVE_USE_YN = "Y";

    private final StoreRepository storeRepository;
    private final StoreDiscountCouponRepository storeDiscountCouponRepository;
    private final AdminUserRepository adminUserRepository;

    @Override
    public StoreSettlementResponse getStoreSettlement(LocalDate fromDate, LocalDate toDate, Long storeId) {
        LocalDateTime start = requireStart(fromDate);
        LocalDateTime end = requireEnd(toDate);
        Long effectiveStoreId = resolveEffectiveStoreId(storeId);

        List<Store> targetStores = effectiveStoreId != null
                ? List.of(storeRepository.findById(effectiveStoreId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상가를 찾을 수 없습니다: " + effectiveStoreId)))
                : storeRepository.findAll();

        Map<Long, Long> issuedCountByStore = toCountMap(storeDiscountCouponRepository.countIssuedGroupedByStore(start, end));
        Map<Long, Object[]> usedByStore = toUsedMap(storeDiscountCouponRepository.countUsedGroupedByStore(start, end));

        List<StoreSettlementItemResponse> items = new ArrayList<>();
        for (Store store : targetStores) {
            long issuedCount = issuedCountByStore.getOrDefault(store.getStoreId(), 0L);
            Object[] used = usedByStore.get(store.getStoreId());
            long usedCount = used != null ? (Long) used[0] : 0L;
            BigDecimal discountAmount = used != null ? (BigDecimal) used[1] : BigDecimal.ZERO;

            items.add(StoreSettlementItemResponse.builder()
                    .storeId(store.getStoreId())
                    .storeName(store.getStoreName())
                    .issuedCouponCount((int) issuedCount)
                    .usedCouponCount((int) usedCount)
                    .discountAmount(discountAmount)
                    .billingAmount(discountAmount)
                    .build());
        }

        return StoreSettlementResponse.builder().items(items).build();
    }

    /** STORE_OWNER면 파라미터를 무시하고 세션 소속 storeId로 강제 치환(design.md [Screen 3] 모듈경계 명시) */
    private Long resolveEffectiveStoreId(Long requestedStoreId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return requestedStoreId;
        }

        AdminUser adminUser = adminUserRepository.findByLoginIdAndUseYn(authentication.getName(), ACTIVE_USE_YN)
                .orElse(null);
        if (adminUser == null || !ROLE_STORE_OWNER.equals(adminUser.getRole())) {
            return requestedStoreId;
        }
        if (adminUser.getStoreId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "소속 상가 정보가 없는 계정입니다.");
        }
        return adminUser.getStoreId();
    }

    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    private Map<Long, Object[]> toUsedMap(List<Object[]> rows) {
        Map<Long, Object[]> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], new Object[] {row[1], row[2]});
        }
        return map;
    }

    private LocalDateTime requireStart(LocalDate fromDate) {
        if (fromDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate는 필수입니다.");
        }
        return fromDate.atStartOfDay();
    }

    private LocalDateTime requireEnd(LocalDate toDate) {
        if (toDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toDate는 필수입니다.");
        }
        return toDate.plusDays(1).atStartOfDay();
    }
}
