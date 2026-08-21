package com.parking.admin.repository;

import com.parking.admin.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 근거: backend-schema.md §1 AUDIT_LOG — insert 외 UPDATE/DELETE 사용 금지(append-only).
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
