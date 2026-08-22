package com.tanm.backend.repository;

import com.tanm.backend.entity.Address;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserAndIsDeletedFalse(AppUser user);
    List<Address> findByGuestTokenAndIsDeletedFalse(String guestToken);
    Optional<Address> findByIdAndUserAndIsDeletedFalse(Long id, AppUser user);
    Optional<Address> findByIdAndGuestTokenAndIsDeletedFalse(Long id, String guestToken);
    Optional<Address> findByIdAndIsDeletedFalse(Long id);
    List<Address> findByUserAndAddressTypeAndIsDefaultTrueAndIsDeletedFalse(AppUser user, AddressType addressType);
    List<Address> findByGuestTokenAndAddressTypeAndIsDefaultTrueAndIsDeletedFalse(String guestToken, AddressType addressType);
}
