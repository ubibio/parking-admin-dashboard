package com.parking.admin.service;

import com.parking.admin.dto.AuditLogCommand;
import com.parking.admin.entity.AuditLog;
import com.parking.admin.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * actorId/actorIp는 클라이언트 입력을 신뢰하지 않고 세션 인증 정보·요청에서 직접 산출한다.
 * 근거: backend-schema.md §1 AUDIT_LOG 비고
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final String UNKNOWN = "UNKNOWN";

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void record(AuditLogCommand command) {
        AuditLog auditLog = AuditLog.builder()
                .actorId(resolveActorId())
                .actorIp(resolveActorIp())
                .actionType(command.getActionType())
                .targetType(command.getTargetType())
                .targetId(command.getTargetId())
                .beforeJson(toJson(command.getBefore()))
                .afterJson(toJson(command.getAfter()))
                .reasonCode(command.getReasonCode())
                .reasonText(command.getReasonText())
                .commandStatus(command.getCommandStatus())
                .build();
        auditLogRepository.save(auditLog);
    }

    private String resolveActorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return UNKNOWN;
        }
        return authentication.getName();
    }

    private String resolveActorIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return UNKNOWN;
        }
        return attributes.getRequest().getRemoteAddr();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            log.error("[AuditLogServiceImpl :: toJson] :: 감사 로그 스냅샷 직렬화 실패 => {}", e.getMessage());
            return null;
        }
    }
}
