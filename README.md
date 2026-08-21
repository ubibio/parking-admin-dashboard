# parking-admin-dashboard

주차관리 관리자 웹 대시보드 — 백엔드 스캐폴드(명령6-0). 화면 도메인(6-1 메인 대시보드, 6-2 입출차 현황,
6-3 매출 정산, 6-4 정기권 관리)은 이 스캐폴드 범위 밖이며 별도로 구현된다.

## 스택
Spring Boot 4.1.0 · Java 21 · Gradle · H2(dev, in-memory) · Spring Data JPA · Lombok · Thymeleaf ·
Spring Security(세션 폼 로그인)

## 실행 방법
```
./gradlew bootRun
```
Windows: `gradlew.bat bootRun`

- 애플리케이션: http://localhost:8080
- 로그인 화면: http://localhost:8080/admin/login
- H2 콘솔(dev): http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:parkingdb`, user: `sa`, password: 공란)

## 개발용 시드 계정
최초 기동 시 `ADMIN_USER` 테이블이 비어 있으면 아래 SUPER_ADMIN 계정 1개가 자동 생성된다
(`com.parking.admin.config.DevDataSeeder`).

| 항목 | 값 |
|---|---|
| loginId | `superadmin` |
| password | `admin1234!` |
| role | `SUPER_ADMIN` |

**운영 배포 전 반드시 비밀번호를 변경할 것.** 이 계정은 개발/로컬 테스트 전용이다.

## 인증 API
| 메서드/경로 | 설명 |
|---|---|
| GET `/admin/login` | 로그인 화면(View) |
| POST `/api/auth/login` | 폼 로그인 처리(`loginId`, `password`). 성공 시 `{ role, userName, redirectUrl }` JSON 반환 |
| POST `/api/auth/logout` | 세션 로그아웃 |

## RBAC
`ADMIN_USER.role` → Spring Security `ROLE_<role>` 권한 매핑. 값: `SUPER_ADMIN` / `SITE_OPERATOR` / `STORE_OWNER`.
메서드 단위 인가(`@PreAuthorize`)는 `SecurityConfig`의 `@EnableMethodSecurity`로 활성화되어 있으며,
실제 `@PreAuthorize` 적용은 각 도메인 컨트롤러(6-1~6-4) 구현 시 수행한다.

## 공용 감사 로그
`AuditLogService`(`com.parking.admin.service`)를 각 도메인 쓰기 API에서 호출해 `AUDIT_LOG` 테이블에 기록한다.
append-only이며 수정/삭제 메서드는 제공하지 않는다. actorId/actorIp는 세션 인증 정보·요청에서 서버가 직접
산출하며 클라이언트 입력을 신뢰하지 않는다.
