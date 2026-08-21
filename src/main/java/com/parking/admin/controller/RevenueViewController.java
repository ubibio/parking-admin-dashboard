package com.parking.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * GET /admin/revenue — 통계 화면 렌더링(View Controller). 콘텐츠 마크업은 frontend-expert 담당(placeholder 템플릿).
 * 근거: design.md [Screen 3] 모듈경계 RevenueViewController
 */
@Controller
public class RevenueViewController {

    @GetMapping("/admin/revenue")
    public String revenue() {
        return "revenue";
    }
}
