package com.parking.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 게이트 마스터 + 실시간 장비 상태. Screen 1 도메인 로직은 이 스캐폴드 범위 밖(6-1).
 * 근거: backend-schema.md §2 GATE
 */
@Entity
@Table(name = "gate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gate_id")
    private Long gateId;

    @Column(name = "gate_name", length = 50, nullable = false)
    private String gateName;

    /** ENTRY / EXIT */
    @Column(name = "gate_type", length = 10, nullable = false)
    private String gateType;

    /** OPEN / CLOSED / FAULT */
    @Column(name = "barrier_status", length = 10, nullable = false)
    private String barrierStatus;

    /** NORMAL / COMM_ERROR */
    @Column(name = "lpr_status", length = 15, nullable = false)
    private String lprStatus;

    /** PRE_PAY / EXIT_PAY — 미보유 게이트는 NULL */
    @Column(name = "pay_station_type", length = 10)
    private String payStationType;

    /** NORMAL / COMM_ERROR / PAPER_LOW */
    @Column(name = "pay_station_status", length = 15)
    private String payStationStatus;

    @Column(name = "last_vehicle_no", length = 20)
    private String lastVehicleNo;

    @Column(name = "last_event_at")
    private LocalDateTime lastEventAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
        if (this.barrierStatus == null) {
            this.barrierStatus = "CLOSED";
        }
        if (this.lprStatus == null) {
            this.lprStatus = "NORMAL";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
