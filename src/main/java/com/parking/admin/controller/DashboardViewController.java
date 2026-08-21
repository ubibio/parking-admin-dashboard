package com.parking.admin.controller;

import com.parking.admin.service.GateStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * GET /admin/dashboard — 대시보드 화면 렌더링, 게이트 목록 초기 주입.
 * 화면 콘텐츠 마크업(KPI 카드/게이트 카드/이벤트 테이블/차트)은 frontend-expert 담당(placeholder 템플릿).
 * 근거: design.md [Screen 1] 모듈경계
 */
@Controller
@RequiredArgsConstructor
public class DashboardViewController {

    private final GateStatusService gateStatusService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("gates", gateStatusService.getGates());
        return "dashboard";
    }
}
