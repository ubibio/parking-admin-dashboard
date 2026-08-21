package com.parking.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * GET /admin/season-passes — 목록 화면 렌더링(View Controller). 콘텐츠 마크업은 frontend-expert 담당(placeholder 템플릿).
 * 근거: design.md [Screen 4] 모듈경계 SeasonPassViewController
 */
@Controller
public class SeasonPassViewController {

    @GetMapping("/admin/season-passes")
    public String seasonPasses() {
        return "season-passes";
    }
}
