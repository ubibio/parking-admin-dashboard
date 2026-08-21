package com.parking.admin.service;

import com.parking.admin.dto.SeasonPassBulkActionFailure;
import com.parking.admin.dto.SeasonPassBulkActionResponse;
import com.parking.admin.dto.SeasonPassCreateResponse;
import com.parking.admin.dto.SeasonPassExtendRequest;
import com.parking.admin.dto.SeasonPassSaveRequest;
import com.parking.admin.dto.SeasonPassUpdateResponse;
import com.parking.admin.entity.SeasonPass;
import com.parking.admin.entity.SeasonPassAllowedGate;
import com.parking.admin.entity.SeasonPassAllowedGateId;
import com.parking.admin.repository.GateRepository;
import com.parking.admin.repository.SeasonPassAllowedGateRepository;
import com.parking.admin.repository.SeasonPassNotificationLogRepository;
import com.parking.admin.repository.SeasonPassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassService, 입출력계약, [상태값] 상태 전이.
 * - allowedGateIds는 SEASON_PASS_ALLOWED_GATE 전체 삭제 후 재삽입 방식으로 갱신한다(비어 있으면 전체 허용 = 행 없음).
 * - passStatus는 저장(생성/수정/연장) 시점에 validTo 기준으로 즉시 재계산한다. 화면 조회 시점 계산에 의존하지
 *   않는다는 [Do NOT]은 "일별 자동 전환(ACTIVE/EXPIRING_SOON/EXPIRED 스윕)"을 스케줄러가 전담해야 한다는 뜻으로 해석했다
 *   (가정 — Issues 참고). 쓰기 시점 즉시 반영은 데이터 정합성을 위해 유지.
 * - AuditLog 기록 없음(brief: "6-0 Gate 참고, AuditLog 불필요 — 감사대상 아님").
 */
@Service
@RequiredArgsConstructor
public class SeasonPassServiceImpl implements SeasonPassService {

    private static final Set<String> ALLOWED_PASS_TYPES =
            Set.of("MONTHLY", "SEMI_ANNUAL", "RESIDENT_EMPLOYEE", "DAY", "NIGHT");
    private static final Set<String> ALLOWED_PAYMENT_STATUSES = Set.of("PAID", "FREE_APPROVED");
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_EXPIRING_SOON = "EXPIRING_SOON";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final int EXPIRING_SOON_WINDOW_DAYS = 7;

    private final SeasonPassRepository seasonPassRepository;
    private final SeasonPassAllowedGateRepository seasonPassAllowedGateRepository;
    private final SeasonPassNotificationLogRepository seasonPassNotificationLogRepository;
    private final GateRepository gateRepository;

    @Override
    @Transactional
    public SeasonPassCreateResponse create(SeasonPassSaveRequest request) {
        validateRequest(request);

        SeasonPass pass = SeasonPass.builder()
                .vehicleNo(request.getVehicleNo())
                .ownerName(request.getOwnerName())
                .phone(request.getPhone())
                .passType(request.getPassType())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .paymentStatus(request.getPaymentStatus())
                .passStatus(resolvePassStatus(request.getValidTo()))
                .build();
        seasonPassRepository.saveAndFlush(pass);

        replaceAllowedGates(pass.getPassId(), request.getAllowedGateIds());

        return SeasonPassCreateResponse.builder().passId(pass.getPassId()).build();
    }

    @Override
    @Transactional
    public SeasonPassUpdateResponse update(Long passId, SeasonPassSaveRequest request) {
        validateRequest(request);
        SeasonPass pass = getOrThrow(passId);

        pass.setVehicleNo(request.getVehicleNo());
        pass.setOwnerName(request.getOwnerName());
        pass.setPhone(request.getPhone());
        pass.setPassType(request.getPassType());
        pass.setValidFrom(request.getValidFrom());
        pass.setValidTo(request.getValidTo());
        pass.setPaymentStatus(request.getPaymentStatus());
        pass.setPassStatus(resolvePassStatus(request.getValidTo()));
        seasonPassRepository.saveAndFlush(pass);

        replaceAllowedGates(passId, request.getAllowedGateIds());

        return SeasonPassUpdateResponse.builder()
                .passId(pass.getPassId())
                .updatedAt(pass.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public SeasonPassBulkActionResponse extend(SeasonPassExtendRequest request) {
        if (request == null || request.getPassIds() == null || request.getPassIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "passIds는 필수입니다.");
        }
        if (request.getNewValidTo() == null && request.getExtendMonths() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "extendMonths 또는 newValidTo 중 하나는 필수입니다.");
        }

        int successCount = 0;
        List<SeasonPassBulkActionFailure> failed = new ArrayList<>();
        for (Long passId : request.getPassIds()) {
            try {
                SeasonPass pass = getOrThrow(passId);
                LocalDate newValidTo = request.getNewValidTo() != null
                        ? request.getNewValidTo()
                        : pass.getValidTo().plusMonths(request.getExtendMonths());
                if (newValidTo.isBefore(pass.getValidTo())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "연장 만료일이 기존 만료일보다 이전일 수 없습니다.");
                }
                pass.setValidTo(newValidTo);
                pass.setPassStatus(resolvePassStatus(newValidTo));
                seasonPassRepository.save(pass);
                successCount++;
            } catch (ResponseStatusException e) {
                failed.add(SeasonPassBulkActionFailure.builder().passId(passId).message(e.getReason()).build());
            } catch (RuntimeException e) {
                failed.add(SeasonPassBulkActionFailure.builder().passId(passId).message(e.getMessage()).build());
            }
        }

        return SeasonPassBulkActionResponse.builder().successCount(successCount).failed(failed).build();
    }

    @Override
    @Transactional
    public SeasonPassBulkActionResponse delete(List<Long> passIds) {
        if (passIds == null || passIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "passIds는 필수입니다.");
        }

        int successCount = 0;
        List<SeasonPassBulkActionFailure> failed = new ArrayList<>();
        for (Long passId : passIds) {
            try {
                getOrThrow(passId);
                seasonPassAllowedGateRepository.deleteByIdPassId(passId);
                seasonPassNotificationLogRepository.deleteByPassId(passId);
                seasonPassRepository.deleteById(passId);
                successCount++;
            } catch (ResponseStatusException e) {
                failed.add(SeasonPassBulkActionFailure.builder().passId(passId).message(e.getReason()).build());
            } catch (RuntimeException e) {
                failed.add(SeasonPassBulkActionFailure.builder().passId(passId).message(e.getMessage()).build());
            }
        }

        return SeasonPassBulkActionResponse.builder().successCount(successCount).failed(failed).build();
    }

    private void validateRequest(SeasonPassSaveRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getVehicleNo())
                || !StringUtils.hasText(request.getOwnerName())
                || !StringUtils.hasText(request.getPhone())
                || !StringUtils.hasText(request.getPassType())
                || request.getValidFrom() == null
                || request.getValidTo() == null
                || !StringUtils.hasText(request.getPaymentStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "vehicleNo/ownerName/phone/passType/validFrom/validTo/paymentStatus는 필수입니다.");
        }
        if (!ALLOWED_PASS_TYPES.contains(request.getPassType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "passType 값이 올바르지 않습니다: " + request.getPassType());
        }
        if (!ALLOWED_PAYMENT_STATUSES.contains(request.getPaymentStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentStatus 값이 올바르지 않습니다: " + request.getPaymentStatus());
        }
        if (request.getValidTo().isBefore(request.getValidFrom())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validTo는 validFrom보다 이전일 수 없습니다.");
        }
        validateAllowedGateIds(request.getAllowedGateIds());
    }

    private void validateAllowedGateIds(List<Long> allowedGateIds) {
        if (allowedGateIds == null || allowedGateIds.isEmpty()) {
            return;
        }
        List<Long> distinctIds = allowedGateIds.stream().distinct().toList();
        long foundCount = gateRepository.findAllById(distinctIds).size();
        if (foundCount != distinctIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "allowedGateIds에 존재하지 않는 게이트가 포함되어 있습니다.");
        }
    }

    private void replaceAllowedGates(Long passId, List<Long> allowedGateIds) {
        seasonPassAllowedGateRepository.deleteByIdPassId(passId);
        if (allowedGateIds == null || allowedGateIds.isEmpty()) {
            return;
        }
        List<SeasonPassAllowedGate> rows = allowedGateIds.stream()
                .distinct()
                .map(gateId -> SeasonPassAllowedGate.builder()
                        .id(new SeasonPassAllowedGateId(passId, gateId))
                        .build())
                .toList();
        seasonPassAllowedGateRepository.saveAll(rows);
    }

    /** validTo 기준 ACTIVE/EXPIRING_SOON(D-7 이내)/EXPIRED 산출. 근거: design.md [Screen 4] [상태값] 상태 전이 */
    private String resolvePassStatus(LocalDate validTo) {
        LocalDate today = LocalDate.now();
        if (validTo.isBefore(today)) {
            return STATUS_EXPIRED;
        }
        if (!validTo.isAfter(today.plusDays(EXPIRING_SOON_WINDOW_DAYS))) {
            return STATUS_EXPIRING_SOON;
        }
        return STATUS_ACTIVE;
    }

    private SeasonPass getOrThrow(Long passId) {
        return seasonPassRepository.findById(passId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "정기권을 찾을 수 없습니다: " + passId));
    }
}
