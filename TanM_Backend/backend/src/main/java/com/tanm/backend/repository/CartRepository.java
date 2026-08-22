package com.tanm.backend.repository;

import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.Cart;
import com.tanm.backend.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "WHERE c.user = :user AND c.status = :status")
    Optional<Cart> findByUserAndStatusWithItems(@Param("user") AppUser user, @Param("status") CartStatus status);

    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "WHERE c.guestToken = :guestToken AND c.status = :status")
    Optional<Cart> findByGuestTokenAndStatusWithItems(@Param("guestToken") String guestToken, @Param("status") CartStatus status);

    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "WHERE c.user = :user AND c.status = :status ORDER BY c.id DESC")
    List<Cart> findByUserAndStatusWithItemsList(@Param("user") AppUser user, @Param("status") CartStatus status);

    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "WHERE c.guestToken = :guestToken AND c.status = :status ORDER BY c.id DESC")
    List<Cart> findByGuestTokenAndStatusWithItemsList(@Param("guestToken") String guestToken, @Param("status") CartStatus status);

    Optional<Cart> findByUserAndStatus(AppUser user, CartStatus status);
    Optional<Cart> findByGuestTokenAndStatus(String guestToken, CartStatus status);
}
