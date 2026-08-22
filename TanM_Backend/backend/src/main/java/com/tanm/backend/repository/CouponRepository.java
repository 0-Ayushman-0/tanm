package com.tanm.backend.repository;

import com.tanm.backend.entity.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Page<Coupon> findAllByIsDeletedFalse(Pageable pageable);

    Optional<Coupon> findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Coupon> findByIdForUpdate(@Param("id") Long id);
}
