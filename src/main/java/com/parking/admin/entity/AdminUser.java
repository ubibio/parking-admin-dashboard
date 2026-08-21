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
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 관리자 계정 — 로그인/RBAC 공용 엔티티.
 * 근거: backend-schema.md §5.5 ADMIN_USER
 */
@Entity
@Table(name = "admin_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", length = 50, nullable = false, unique = true)
    private String loginId;

    @Column(name = "password_hash", length = 100, nullable = false)
    private String passwordHash;

    @Column(name = "user_name", length = 50, nullable = false)
    private String userName;

    /** SUPER_ADMIN / SITE_OPERATOR / STORE_OWNER */
    @Column(name = "role", length = 20, nullable = false)
    private String role;

    /** role=STORE_OWNER일 때만 값 존재 — 세션 소속 판별 키 */
    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "use_yn", length = 1, nullable = false)
    private String useYn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.useYn == null) {
            this.useYn = "Y";
        }
    }
}
