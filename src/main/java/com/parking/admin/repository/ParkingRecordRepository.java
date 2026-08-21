package com.parking.admin.repository;

import com.parking.admin.entity.ParkingRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 근거: backend-schema.md §2 PARKING_RECORD. Screen 1 조회(KPI/이벤트/트래픽)에 필요한 파생 조회만 포함.
 * Screen 2·3 전용 검색·집계 메서드는 해당 화면 구현(6-2, 6-3) 범위.
 * JpaSpecificationExecutor는 Screen 2(6-2) 다중 조건 검색을 위해 추가(Screen 1 메서드는 무변경).
 */
public interface ParkingRecordRepository extends JpaRepository<ParkingRecord, Long>,
        JpaSpecificationExecutor<ParkingRecord> {

    /** 현재 주차 중(출차 미처리) 차량 수 — KPI occupiedCount */
    long countByExitAtIsNull();

    long countByEntryAtBetween(LocalDateTime start, LocalDateTime end);

    long countByExitAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.feeAmount), 0) FROM ParkingRecord p WHERE p.exitAt BETWEEN :start AND :end")
    BigDecimal sumFeeAmountByExitAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 최신 입차 발생(occurrence) N건 — 이벤트 로그 조립용 */
    List<ParkingRecord> findByOrderByEntryAtDesc(Pageable pageable);

    /** 최신 출차 발생(occurrence) N건 — 이벤트 로그 조립용 */
    List<ParkingRecord> findByExitAtIsNotNullOrderByExitAtDesc(Pageable pageable);

    /** 지정일 시간대별 입차 추이 집계용(그룹핑은 서비스 계층에서 수행) */
    List<ParkingRecord> findByEntryAtBetween(LocalDateTime start, LocalDateTime end);

    /** 지정일 시간대별 출차 추이 집계용(그룹핑은 서비스 계층에서 수행) */
    List<ParkingRecord> findByExitAtBetween(LocalDateTime start, LocalDateTime end);

    /** 미인식 차량 알림 판정용(최근 window 내 UNRECOGNIZED 건) */
    List<ParkingRecord> findByRecordStatusAndEntryAtAfter(String recordStatus, LocalDateTime after);
}
