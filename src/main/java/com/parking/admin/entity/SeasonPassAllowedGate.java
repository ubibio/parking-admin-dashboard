package com.parking.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 정기권별 허용 출입 게이트(allowedGateIds[]). 행이 없으면 전체 게이트 허용(design.md [Screen 4] [상태값]).
 * 근거: backend-schema.md §5 SEASON_PASS_ALLOWED_GATE
 */
@Entity
@Table(name = "season_pass_allowed_gate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassAllowedGate {

    @EmbeddedId
    private SeasonPassAllowedGateId id;
}
