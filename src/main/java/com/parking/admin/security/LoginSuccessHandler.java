package com.parking.admin.security;

import com.parking.admin.entity.AdminUser;
import com.parking.admin.repository.AdminUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * POST /api/auth/login 성공 응답을 JSON { role, userName, redirectUrl }로 반환한다(jQuery AJAX 호출 전제).
 * 근거: backend-schema.md §5.5 API, design.md 공통 규칙(jQuery AJAX)
 *
 * 가정(Assumption): redirectUrl은 화면 도메인(6-1~6-4)이 아직 구현되지 않아 "/admin/dashboard" 고정값을 반환한다.
 * 역할별 실제 랜딩 페이지는 해당 화면 구현 시 재검토가 필요하다.
 */
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String ACTIVE_USE_YN = "Y";
    private static final String DEFAULT_REDIRECT_URL = "/admin/dashboard";

    private final AdminUserRepository adminUserRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        AdminUser adminUser = adminUserRepository.findByLoginIdAndUseYn(authentication.getName(), ACTIVE_USE_YN)
                .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다: " + authentication.getName()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("role", adminUser.getRole());
        body.put("userName", adminUser.getUserName());
        body.put("redirectUrl", DEFAULT_REDIRECT_URL);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
