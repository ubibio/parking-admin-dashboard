package com.parking.admin.repository;

import com.parking.admin.entity.SeasonPassAllowedGate;
import com.parking.admin.entity.SeasonPassAllowedGateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 근거: backend-schema.md §5 SEASON_PASS_ALLOWED_GATE
 */
public interface SeasonPassAllowedGateRepository extends JpaRepository<SeasonPassAllowedGate, SeasonPassAllowedGateId> {

    List<SeasonPassAllowedGate> findByIdPassId(Long passId);

    void deleteByIdPassId(Long passId);
}
