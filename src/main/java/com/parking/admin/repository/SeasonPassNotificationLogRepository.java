package com.parking.admin.repository;

import com.parking.admin.entity.SeasonPassNotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 근거: backend-schema.md §5 SEASON_PASS_NOTIFICATION_LOG
 * dDay 프로퍼티명이 파생 쿼리 메서드명 파싱에서 모호할 수 있어 명시적 JPQL을 사용한다.
 */
public interface SeasonPassNotificationLogRepository extends JpaRepository<SeasonPassNotificationLog, Long> {

    /** 스케줄러 중복 발송 방지(동일 정기권·동일 D-day에 대해 이미 발송 성공 이력이 있는지) */
    @Query("SELECT COUNT(l) > 0 FROM SeasonPassNotificationLog l "
            + "WHERE l.passId = :passId AND l.dDay = :dDay AND l.notifyStatus = :notifyStatus")
    boolean existsSentLog(@Param("passId") Long passId, @Param("dDay") Integer dDay, @Param("notifyStatus") String notifyStatus);

    void deleteByPassId(Long passId);
}
