package com.parking.admin.repository;

import com.parking.admin.entity.SeasonPass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

/**
 * 근거: backend-schema.md §5 SEASON_PASS.
 * JpaSpecificationExecutor는 목록 키워드/상태/구분 검색용(SeasonPassSearchService).
 */
public interface SeasonPassRepository extends JpaRepository<SeasonPass, Long>,
        JpaSpecificationExecutor<SeasonPass> {

    /** D-7/D-3 알림 대상(validTo가 정확히 오늘+dDay인 건, 이미 만료 처리된 건 제외) — SeasonPassExpiryScheduler */
    List<SeasonPass> findByValidToAndPassStatusNot(LocalDate validTo, String passStatus);

    /** 만료 전환 대상(validTo 경과, 아직 EXPIRED 아님) — SeasonPassExpiryScheduler */
    List<SeasonPass> findByValidToBeforeAndPassStatusNot(LocalDate validTo, String passStatus);

    /** ACTIVE -> EXPIRING_SOON 전환 대상(오늘부터 validTo까지 7일 이내) — SeasonPassExpiryScheduler */
    List<SeasonPass> findByPassStatusAndValidToBetween(String passStatus, LocalDate start, LocalDate end);
}
