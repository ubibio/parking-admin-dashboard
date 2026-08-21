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
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 정기권. ownerName/phone은 원본 저장하고, 마스킹은 DTO 직렬화 단계(SeasonPassSearchServiceImpl)에서 수행.
 * FK는 Gate와 동일하게 단순 Long 컬럼으로 두고 연관관계 매핑은 하지 않는다(기존 관례, SeasonPassAllowedGate로 다대다 연결).
 * 근거: backend-schema.md §5 SEASON_PASS
 */
@Entity
@Table(name = "season_pass", indexes = {
        @Index(name = "idx_season_pass_vehicle_no", columnList = "vehicle_no"),
        @Index(name = "idx_season_pass_valid_to", columnList = "valid_to")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pass_id")
    private Long passId;

    @Column(name = "vehicle_no", length = 20, nullable = false)
    private String vehicleNo;

    /** 원본 저장 — 마스킹은 조회 응답 DTO 변환 단계에서 수행 */
    @Column(name = "owner_name", length = 50, nullable = false)
    private String ownerName;

    /** 원본 저장 — 마스킹은 조회 응답 DTO 변환 단계에서 수행 */
    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    /** MONTHLY / SEMI_ANNUAL / RESIDENT_EMPLOYEE / DAY / NIGHT */
    @Column(name = "pass_type", length = 20, nullable = false)
    private String passType;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    /** PAID / FREE_APPROVED */
    @Column(name = "payment_status", length = 15, nullable = false)
    private String paymentStatus;

    /** ACTIVE / EXPIRING_SOON / EXPIRED */
    @Column(name = "pass_status", length = 15, nullable = false)
    private String passStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.passStatus == null) {
            this.passStatus = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
