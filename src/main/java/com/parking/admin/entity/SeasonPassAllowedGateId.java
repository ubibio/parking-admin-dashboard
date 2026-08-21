package com.parking.admin.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 * SEASON_PASS_ALLOWED_GATE 복합키(pass_id, gate_id).
 * 근거: backend-schema.md §5 SEASON_PASS_ALLOWED_GATE
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SeasonPassAllowedGateId implements Serializable {

    @Column(name = "pass_id")
    private Long passId;

    @Column(name = "gate_id")
    private Long gateId;
}
