package com.tanm.backend.service;

import com.tanm.backend.dto.AddressCreateRequest;
import com.tanm.backend.dto.AddressDto;
import com.tanm.backend.entity.AppUser;

import java.util.List;

public interface AddressService {
    List<AddressDto> getMyAddresses(AppUser user, String guestToken);
    AddressDto getAddressById(Long id, AppUser user, String guestToken);
    AddressDto createAddress(AddressCreateRequest request, AppUser user, String guestToken);
    AddressDto updateAddress(Long id, AddressCreateRequest request, AppUser user, String guestToken);
    void deleteAddress(Long id, AppUser user, String guestToken);
    AddressDto setDefaultAddress(Long id, AppUser user, String guestToken);

    default List<AddressDto> getMyAddresses(AppUser user) { return getMyAddresses(user, null); }
    default AddressDto getAddressById(Long id, AppUser user) { return getAddressById(id, user, null); }
    default AddressDto createAddress(AddressCreateRequest request, AppUser user) { return createAddress(request, user, null); }
    default AddressDto updateAddress(Long id, AddressCreateRequest request, AppUser user) { return updateAddress(id, request, user, null); }
    default void deleteAddress(Long id, AppUser user) { deleteAddress(id, user, null); }
    default AddressDto setDefaultAddress(Long id, AppUser user) { return setDefaultAddress(id, user, null); }
}
