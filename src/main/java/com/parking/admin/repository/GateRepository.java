package com.parking.admin.repository;

import com.parking.admin.entity.Gate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 근거: backend-schema.md §2 GATE. 조회/제어 로직은 6-1 범위.
 */
public interface GateRepository extends JpaRepository<Gate, Long> {

    /** 알림 판정(차단기 장시간 열림)용 */
    List<Gate> findByBarrierStatus(String barrierStatus);

    /** 알림 판정(정산기 용지 부족)용 */
    List<Gate> findByPayStationStatus(String payStationStatus);
}
