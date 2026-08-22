package com.tanm.backend.repository;

import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items " +
           "LEFT JOIN FETCH o.user " +
           "WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items " +
           "LEFT JOIN FETCH o.user " +
           "WHERE o.user = :user " +
           "ORDER BY o.orderedAt DESC")
    List<Order> findByUserWithItemsOrderByOrderedAtDesc(@Param("user") AppUser user);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items " +
           "LEFT JOIN FETCH o.user " +
           "ORDER BY o.orderedAt DESC")
    List<Order> findAllWithItemsOrderByOrderedAtDesc();

    @Query(value = "SELECT o FROM Order o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.user",
           countQuery = "SELECT COUNT(o) FROM Order o")
    org.springframework.data.domain.Page<Order> findAllWithItems(org.springframework.data.domain.Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);
    org.springframework.data.domain.Page<Order> findByUserAndIsDeletedFalse(AppUser user, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Order> findByGuestTokenAndIsDeletedFalse(String guestToken, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items item " +
           "WHERE o.user = :user AND item.product.id = :productId " +
           "AND o.paymentStatus = com.tanm.backend.enums.PaymentStatus.PAID " +
           "AND o.isDeleted = false")
    boolean existsVerifiedPurchase(@Param("user") AppUser user, @Param("productId") Long productId);
}
