package com.parking.admin.repository;

import com.parking.admin.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 근거: backend-schema.md §4 STORE. 정산 로직은 6-3 범위.
 */
public interface StoreRepository extends JpaRepository<Store, Long> {
}
