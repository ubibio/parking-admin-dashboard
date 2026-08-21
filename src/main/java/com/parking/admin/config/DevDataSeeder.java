package com.parking.admin.config;

import com.parking.admin.entity.AdminUser;
import com.parking.admin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 개발용(H2) SUPER_ADMIN 계정 시드. 최초 기동 시 계정이 하나도 없으면 1개 생성한다.
 * 초기 비밀번호 값은 로그에 남기지 않는다(평문 로그 금지) — README.md에 문서화되어 있다.
 * 근거: brief Constraints "H2 seed로 SUPER_ADMIN 1개 생성(개발용, 문서화)"
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private static final String SEED_LOGIN_ID = "superadmin";
    private static final String SEED_PASSWORD = "admin1234!";

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.count() > 0) {
            return;
        }

        AdminUser superAdmin = AdminUser.builder()
                .loginId(SEED_LOGIN_ID)
                .passwordHash(passwordEncoder.encode(SEED_PASSWORD))
                .userName("최고관리자")
                .role("SUPER_ADMIN")
                .useYn("Y")
                .build();
        adminUserRepository.save(superAdmin);

        log.warn("[DevDataSeeder :: run] :: 개발용 SUPER_ADMIN 계정 생성 완료 => loginId={} (초기 비밀번호는 README.md 참고, 운영 배포 전 반드시 변경할 것)",
                SEED_LOGIN_ID);
    }
}
