package com.parking.admin.controller;

import com.parking.admin.entity.AdminUser;
import com.parking.admin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 공통 레이아웃(fragments/layout.html topbar 관리자 드롭다운)에 표시할 로그인 사용자 표시명을
 * 모든 화면 컨트롤러에 "adminName" 모델 속성으로 주입한다.
 * 근거: Thymeleaf 최신 버전은 템플릿에서 #request/#httpServletRequest 유틸리티 객체를
 * 기본 제공하지 않으므로(IllegalArgumentException), Authentication을 컨트롤러 계층에서
 * 모델 속성으로 명시 주입하는 방식을 사용한다.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private static final String ACTIVE_USE_YN = "Y";

    private final AdminUserRepository adminUserRepository;

    @ModelAttribute("adminName")
    public String adminName(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return adminUserRepository.findByLoginIdAndUseYn(authentication.getName(), ACTIVE_USE_YN)
                .map(AdminUser::getUserName)
                .orElse(authentication.getName());
    }
}
