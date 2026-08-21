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
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 공용 감사 로그 — append-only(수정/삭제 API 없음).
 * 근거: backend-schema.md §1 AUDIT_LOG, design.md 비기능요구사항 §3
 */
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_log_target", columnList = "target_type, target_id, created_at"),
        @Index(name = "idx_audit_log_action", columnList = "action_type, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "actor_id", length = 64, nullable = false)
    private String actorId;

    @Column(name = "actor_ip", length = 45, nullable = false)
    private String actorIp;

    /** BARRIER_OPEN / LPR_REBOOT / MANUAL_PAYMENT / PLATE_CORRECTION / FORCE_EXIT */
    @Column(name = "action_type", length = 40, nullable = false)
    private String actionType;

    /** GATE / PARKING_RECORD */
    @Column(name = "target_type", length = 30, nullable = false)
    private String targetType;

    @Column(name = "target_id", length = 64, nullable = false)
    private String targetId;

    @Lob
    @Column(name = "before_json")
    private String beforeJson;

    @Lob
    @Column(name = "after_json")
    private String afterJson;

    @Column(name = "reason_code", length = 40)
    private String reasonCode;

    @Column(name = "reason_text", length = 500)
    private String reasonText;

    /** ACCEPTED / FAILED — 명령 실패도 기록 */
    @Column(name = "command_status", length = 10, nullable = false)
    private String commandStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
