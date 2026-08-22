package com.tanm.backend.service.impl;

import com.tanm.backend.dto.AddressCreateRequest;
import com.tanm.backend.dto.AddressDto;
import com.tanm.backend.entity.Address;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.enums.AddressType;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.mapper.AddressMapper;
import com.tanm.backend.repository.AddressRepository;
import com.tanm.backend.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AddressDto> getMyAddresses(AppUser user, String guestToken) {
        List<Address> list;
        if (user != null) {
            list = addressRepository.findByUserAndIsDeletedFalse(user);
        } else if (guestToken != null && !guestToken.isBlank()) {
            list = addressRepository.findByGuestTokenAndIsDeletedFalse(guestToken);
        } else {
            list = java.util.List.of();
        }
        return list.stream().map(addressMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDto getAddressById(Long id, AppUser user, String guestToken) {
        Address address = findAddress(id, user, guestToken);
        return addressMapper.toDto(address);
    }

    @Override
    @Transactional
    public AddressDto createAddress(AddressCreateRequest request, AppUser user, String guestToken) {
        Address address = addressMapper.toEntity(request);
        if (user != null) {
            address.setUser(user);
        } else {
            address.setGuestToken(guestToken);
        }

        if (request.isDefault()) {
            resetOtherDefaults(user, guestToken, request.getAddressType());
        }

        Address saved = addressRepository.save(address);
        return addressMapper.toDto(saved);
    }

    @Override
    @Transactional
    public AddressDto updateAddress(Long id, AddressCreateRequest request, AppUser user, String guestToken) {
        Address address = findAddress(id, user, guestToken);

        if (request.isDefault()) {
            resetOtherDefaults(user, guestToken, request.getAddressType());
        }

        addressMapper.updateEntity(request, address);
        Address saved = addressRepository.save(address);
        return addressMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id, AppUser user, String guestToken) {
        Address address = findAddress(id, user, guestToken);
        address.setDeleted(true);
        addressRepository.save(address);
    }

    @Override
    @Transactional
    public AddressDto setDefaultAddress(Long id, AppUser user, String guestToken) {
        Address address = findAddress(id, user, guestToken);

        resetOtherDefaults(user, guestToken, address.getAddressType());
        address.setDefault(true);
        Address saved = addressRepository.save(address);
        return addressMapper.toDto(saved);
    }

    private Address findAddress(Long id, AppUser user, String guestToken) {
        java.util.Optional<Address> opt = java.util.Optional.empty();
        if (user != null) {
            opt = addressRepository.findByIdAndUserAndIsDeletedFalse(id, user);
        } else if (guestToken != null && !guestToken.isBlank()) {
            opt = addressRepository.findByIdAndGuestTokenAndIsDeletedFalse(id, guestToken);
        }
        if (opt.isEmpty()) {
            opt = addressRepository.findByIdAndIsDeletedFalse(id);
        }
        return opt.orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
    }

    private void resetOtherDefaults(AppUser user, String guestToken, AddressType type) {
        List<Address> defaults;
        if (user != null) {
            defaults = addressRepository.findByUserAndAddressTypeAndIsDefaultTrueAndIsDeletedFalse(user, type);
        } else if (guestToken != null && !guestToken.isBlank()) {
            defaults = addressRepository.findByGuestTokenAndAddressTypeAndIsDefaultTrueAndIsDeletedFalse(guestToken, type);
        } else {
            defaults = java.util.List.of();
        }
        for (Address addr : defaults) {
            addr.setDefault(false);
            addressRepository.save(addr);
        }
    }
}
