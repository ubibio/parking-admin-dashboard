package com.parking.admin.service;

import com.parking.admin.dto.SeasonPassBulkActionFailure;
import com.parking.admin.dto.SeasonPassNotificationRequest;
import com.parking.admin.dto.SeasonPassNotificationResponse;
import com.parking.admin.entity.SeasonPass;
import com.parking.admin.entity.SeasonPassNotificationLog;
import com.parking.admin.gateway.MessagingGateway;
import com.parking.admin.repository.SeasonPassNotificationLogRepository;
import com.parking.admin.repository.SeasonPassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassNotificationService, 입출력계약 POST notifications,
 * 스케줄러 동작("매일 1회 validTo-7일/3일 대상 자동 발송").
 * - 수동(passIds[]) 발송의 dDay는 NULL, dDay 조건 기반 발송(관리자 수동 또는 스케줄러)은 dDay를 기록한다(backend-schema.md §5).
 * - 스케줄러 발송의 기본 채널은 design.md에 명시가 없어 ALIM_TALK로 가정한다(가정 — Issues 참고).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonPassNotificationServiceImpl implements SeasonPassNotificationService {

    private static final Set<String> ALLOWED_CHANNELS = Set.of("ALIM_TALK", "SMS");
    private static final Set<Integer> ALLOWED_D_DAYS = Set.of(7, 3);
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final String NOTIFY_STATUS_SENT = "SENT";
    private static final String NOTIFY_STATUS_FAILED = "FAILED";
    private static final String SCHEDULE_DEFAULT_CHANNEL = "ALIM_TALK";
    private static final String UNKNOWN_ACTOR = null;

    private final SeasonPassRepository seasonPassRepository;
    private final SeasonPassNotificationLogRepository seasonPassNotificationLogRepository;
    private final MessagingGateway messagingGateway;

    @Override
    @Transactional
    public SeasonPassNotificationResponse notify(SeasonPassNotificationRequest request) {
        if (request == null || !StringUtils.hasText(request.getChannel()) || !ALLOWED_CHANNELS.contains(request.getChannel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "channel은 ALIM_TALK 또는 SMS 중 하나여야 합니다.");
        }

        List<SeasonPass> targets;
        Integer dDay = null;
        if (request.getPassIds() != null && !request.getPassIds().isEmpty()) {
            targets = seasonPassRepository.findAllById(request.getPassIds());
        } else if (request.getDDay() != null) {
            if (!ALLOWED_D_DAYS.contains(request.getDDay())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dDay는 7 또는 3이어야 합니다.");
            }
            dDay = request.getDDay();
            targets = seasonPassRepository.findByValidToAndPassStatusNot(LocalDate.now().plusDays(dDay), STATUS_EXPIRED);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "passIds 또는 dDay 중 하나는 필수입니다.");
        }

        String actorId = resolveActorId();
        int requestedCount = request.getPassIds() != null && !request.getPassIds().isEmpty()
                ? request.getPassIds().size()
                : targets.size();

        List<SeasonPassBulkActionFailure> failed = new ArrayList<>();
        if (request.getPassIds() != null) {
            appendMissingPassFailures(request.getPassIds(), targets, failed);
        }

        int sentCount = 0;
        for (SeasonPass pass : targets) {
            boolean success = sendAndLog(pass, request.getChannel(), dDay, actorId);
            if (success) {
                sentCount++;
            } else {
                failed.add(SeasonPassBulkActionFailure.builder()
                        .passId(pass.getPassId())
                        .message("알림 발송에 실패했습니다.")
                        .build());
            }
        }

        return SeasonPassNotificationResponse.builder()
                .requestedCount(requestedCount)
                .sentCount(sentCount)
                .failed(failed)
                .build();
    }

    @Override
    @Transactional
    public void notifyExpiringSoonForSchedule(int dDay) {
        List<SeasonPass> targets = seasonPassRepository.findByValidToAndPassStatusNot(
                LocalDate.now().plusDays(dDay), STATUS_EXPIRED);

        for (SeasonPass pass : targets) {
            if (seasonPassNotificationLogRepository.existsSentLog(pass.getPassId(), dDay, NOTIFY_STATUS_SENT)) {
                continue;
            }
            sendAndLog(pass, SCHEDULE_DEFAULT_CHANNEL, dDay, UNKNOWN_ACTOR);
        }
    }

    private boolean sendAndLog(SeasonPass pass, String channel, Integer dDay, String requestedBy) {
        String message = buildMessage(pass, dDay);
        boolean success = messagingGateway.send(pass.getPhone(), channel, message);

        SeasonPassNotificationLog logEntry = SeasonPassNotificationLog.builder()
                .passId(pass.getPassId())
                .notifyChannel(channel)
                .notifyStatus(success ? NOTIFY_STATUS_SENT : NOTIFY_STATUS_FAILED)
                .dDay(dDay)
                .requestedBy(requestedBy)
                .sentAt(success ? LocalDateTime.now() : null)
                .build();
        seasonPassNotificationLogRepository.save(logEntry);

        return success;
    }

    private String buildMessage(SeasonPass pass, Integer dDay) {
        if (dDay != null) {
            return "정기권(" + pass.getVehicleNo() + ") 만료 D-" + dDay + "일 안내드립니다. 만료일: " + pass.getValidTo();
        }
        return "정기권(" + pass.getVehicleNo() + ") 안내드립니다. 만료일: " + pass.getValidTo();
    }

    private void appendMissingPassFailures(List<Long> requestedIds, List<SeasonPass> found, List<SeasonPassBulkActionFailure> failed) {
        Set<Long> foundIds = found.stream().map(SeasonPass::getPassId).collect(Collectors.toSet());
        for (Long passId : requestedIds) {
            if (!foundIds.contains(passId)) {
                failed.add(SeasonPassBulkActionFailure.builder()
                        .passId(passId)
                        .message("정기권을 찾을 수 없습니다: " + passId)
                        .build());
            }
        }
    }

    /** AuditLogServiceImpl.resolveActorId()와 동일한 방식(SecurityContextHolder) — AuditLog 대상 아니라 별도 기록 없음 */
    private String resolveActorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return UNKNOWN_ACTOR;
        }
        return authentication.getName();
    }
}
