package com.parking.admin.security;

import com.parking.admin.entity.AdminUser;
import com.parking.admin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 세션 폼 로그인용 UserDetailsService. role → "ROLE_" 접두 GrantedAuthority 매핑.
 * 근거: backend-schema.md §5.5, design.md 비기능요구사항 §2 RBAC
 */
@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private static final String ACTIVE_USE_YN = "Y";

    private final AdminUserRepository adminUserRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserRepository.findByLoginIdAndUseYn(loginId, ACTIVE_USE_YN)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않거나 비활성화된 계정입니다: " + loginId));

        return User.builder()
                .username(adminUser.getLoginId())
                .password(adminUser.getPasswordHash())
                .authorities("ROLE_" + adminUser.getRole())
                .build();
    }
}
