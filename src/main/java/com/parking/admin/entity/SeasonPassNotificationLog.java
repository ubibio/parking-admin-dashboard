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
 * D-7/D-3 알림 발송 이력. 스케줄러 중복 발송 방지 및 notifyStatus 추적 근거.
 * FK는 SeasonPass와 동일하게 단순 Long 컬럼으로 두고 연관관계 매핑은 하지 않는다(기존 관례).
 * 근거: backend-schema.md §5 SEASON_PASS_NOTIFICATION_LOG
 */
@Entity
@Table(name = "season_pass_notification_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPassNotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "pass_id", nullable = false)
    private Long passId;

    /** ALIM_TALK / SMS */
    @Column(name = "notify_channel", length = 15, nullable = false)
    private String notifyChannel;

    /** PENDING / SENT / FAILED */
    @Column(name = "notify_status", length = 10, nullable = false)
    private String notifyStatus;

    /** 7 또는 3. 수동 발송(passIds[] 지정) 시 NULL */
    @Column(name = "d_day")
    private Integer dDay;

    /** 수동 발송 시 관리자 로그인ID. 배치(스케줄러)는 NULL */
    @Column(name = "requested_by", length = 64)
    private String requestedBy;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
