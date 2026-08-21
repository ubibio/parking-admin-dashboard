package com.parking.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * GET /admin/login — 로그인 화면 렌더링(View Controller). 인증 불필요.
 * 근거: backend-schema.md §5.5
 */
@Controller
public class LoginViewController {

    @GetMapping("/admin/login")
    public String loginPage() {
        return "login";
    }
}
