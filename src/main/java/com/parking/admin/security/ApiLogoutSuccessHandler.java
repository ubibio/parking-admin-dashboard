package com.parking.admin.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * POST /api/auth/logout 성공 시 리다이렉트 없이 200만 응답한다(AJAX 호출 전제).
 */
@Component
public class ApiLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                 Authentication authentication) {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
