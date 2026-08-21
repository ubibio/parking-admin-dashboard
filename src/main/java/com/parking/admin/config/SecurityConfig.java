package com.parking.admin.config;

import com.parking.admin.security.ApiLogoutSuccessHandler;
import com.parking.admin.security.LoginFailureHandler;
import com.parking.admin.security.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 세션 폼 로그인 + RBAC(3단계: SUPER_ADMIN/SITE_OPERATOR/STORE_OWNER).
 * 근거: backend-schema.md §5.5, design.md 비기능요구사항 §2
 * - URL 단위 인가는 여기서, 명령 API 메서드 단위 인가(@PreAuthorize)는 각 도메인 컨트롤러(6-1~6-4)에서 적용한다(EnableMethodSecurity로 활성화만 이 스캐폴드가 담당).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final ApiLogoutSuccessHandler apiLogoutSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(apiLogoutSuccessHandler)
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/ws/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
