package com.parking.admin.controller;

import com.parking.admin.dto.SeasonPassBulkActionResponse;
import com.parking.admin.dto.SeasonPassCreateResponse;
import com.parking.admin.dto.SeasonPassDeleteRequest;
import com.parking.admin.dto.SeasonPassDetailResponse;
import com.parking.admin.dto.SeasonPassExtendRequest;
import com.parking.admin.dto.SeasonPassListResponse;
import com.parking.admin.dto.SeasonPassNotificationRequest;
import com.parking.admin.dto.SeasonPassNotificationResponse;
import com.parking.admin.dto.SeasonPassSaveRequest;
import com.parking.admin.dto.SeasonPassSearchCondition;
import com.parking.admin.dto.SeasonPassUpdateResponse;
import com.parking.admin.service.SeasonPassNotificationService;
import com.parking.admin.service.SeasonPassSearchService;
import com.parking.admin.service.SeasonPassService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 정기권 검색·등록·수정·연장·삭제 + 만료 알림 수동/조건 발송.
 * 쓰기 작업(등록/수정/연장/삭제/알림발송)은 SUPER_ADMIN 전용 — design.md 비기능요구사항 §2 RBAC 표에서
 * SITE_OPERATOR/STORE_OWNER 권한 범위에 Screen 4가 포함되지 않아 SUPER_ADMIN 전용으로 판단했다(가정 — Issues 참고).
 * notifications 엔드포인트는 design.md 모듈경계 표에 소속 컨트롤러가 명시되지 않아, 동일 `/api/season-passes` 베이스를
 * 쓰는 이 컨트롤러에 포함했다(가정 — Issues 참고).
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassApiController, 입출력계약, backend-schema.md §5 API
 */
@RestController
@RequestMapping("/api/season-passes")
@RequiredArgsConstructor
public class SeasonPassApiController {

    private final SeasonPassSearchService seasonPassSearchService;
    private final SeasonPassService seasonPassService;
    private final SeasonPassNotificationService seasonPassNotificationService;

    @GetMapping
    public SeasonPassListResponse list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String passType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        SeasonPassSearchCondition condition = SeasonPassSearchCondition.builder()
                .keyword(keyword)
                .status(status)
                .passType(passType)
                .build();
        return seasonPassSearchService.search(condition, page, size);
    }

    /** 수정 폼 초기값 채우기 전용 — ownerName/phone 마스킹 없이 원본 반환. 근거: 6-4 Issue#1 보완 */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{passId}")
    public SeasonPassDetailResponse getDetail(@PathVariable Long passId) {
        return seasonPassSearchService.getDetail(passId);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public SeasonPassCreateResponse create(@RequestBody SeasonPassSaveRequest request) {
        return seasonPassService.create(request);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{passId}")
    public SeasonPassUpdateResponse update(@PathVariable Long passId, @RequestBody SeasonPassSaveRequest request) {
        return seasonPassService.update(passId, request);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/extend")
    public SeasonPassBulkActionResponse extend(@RequestBody SeasonPassExtendRequest request) {
        return seasonPassService.extend(request);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping
    public SeasonPassBulkActionResponse delete(@RequestBody SeasonPassDeleteRequest request) {
        return seasonPassService.delete(request == null ? null : request.getPassIds());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/notifications")
    public SeasonPassNotificationResponse notify(@RequestBody SeasonPassNotificationRequest request) {
        return seasonPassNotificationService.notify(request);
    }
}
