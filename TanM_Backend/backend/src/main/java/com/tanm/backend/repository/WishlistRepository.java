package com.tanm.backend.repository;

import com.tanm.backend.entity.AppUser;
import com.tanm.backend.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUser(AppUser user);
    Optional<Wishlist> findByUserId(Long userId);
}
