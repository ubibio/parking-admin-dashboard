package com.parking.admin.repository;

import com.parking.admin.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 근거: backend-schema.md §5.5 ADMIN_USER
 */
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByLoginIdAndUseYn(String loginId, String useYn);
}
