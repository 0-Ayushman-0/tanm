package com.tanm.backend.service.impl;

import com.tanm.backend.dto.AddressCreateRequest;
import com.tanm.backend.dto.AddressDto;
import com.tanm.backend.entity.Address;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.enums.AddressType;
import com.tanm.backend.mapper.AddressMapper;
import com.tanm.backend.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private AppUser user;
    private Address existingDefaultAddress;
    private AddressCreateRequest newDefaultRequest;
    private Address newAddress;
    private AddressDto newAddressDto;

    @BeforeEach
    void setUp() {
        user = AppUser.builder().email("john@example.com").build();

        existingDefaultAddress = Address.builder()
                .fullName("John Doe")
                .addressType(AddressType.SHIPPING)
                .isDefault(true)
                .build();

        newDefaultRequest = AddressCreateRequest.builder()
                .fullName("Jane Doe")
                .addressType(AddressType.SHIPPING)
                .isDefault(true)
                .build();

        newAddress = Address.builder()
                .fullName("Jane Doe")
                .addressType(AddressType.SHIPPING)
                .isDefault(true)
                .build();

        newAddressDto = AddressDto.builder()
                .id(2L)
                .fullName("Jane Doe")
                .addressType(AddressType.SHIPPING)
                .isDefault(true)
                .build();
    }

    @Test
    void createAddress_whenDefault_shouldResetOtherDefaults() {
        // Arrange
        Mockito.when(addressMapper.toEntity(any(AddressCreateRequest.class))).thenReturn(newAddress);
        Mockito.when(addressRepository.findByUserAndAddressTypeAndIsDefaultTrueAndIsDeletedFalse(eq(user), eq(AddressType.SHIPPING)))
                .thenReturn(Collections.singletonList(existingDefaultAddress));
        Mockito.when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(addressMapper.toDto(any(Address.class))).thenReturn(newAddressDto);

        // Act
        AddressDto result = addressService.createAddress(newDefaultRequest, user);

        // Assert
        assertNotNull(result);
        assertTrue(result.isDefault());
        assertFalse(existingDefaultAddress.isDefault()); // Switched to false

        Mockito.verify(addressRepository).save(existingDefaultAddress);
        Mockito.verify(addressRepository).save(newAddress);
    }

    @Test
    void setDefaultAddress_shouldResetOtherDefaultsAndSetNewDefault() {
        // Arrange
        Address targetAddress = Address.builder()
                .fullName("Jane Doe")
                .addressType(AddressType.SHIPPING)
                .isDefault(false)
                .build();

        Mockito.when(addressRepository.findByIdAndUserAndIsDeletedFalse(eq(2L), eq(user)))
                .thenReturn(Optional.of(targetAddress));
        Mockito.when(addressRepository.findByUserAndAddressTypeAndIsDefaultTrueAndIsDeletedFalse(eq(user), eq(AddressType.SHIPPING)))
                .thenReturn(Collections.singletonList(existingDefaultAddress));
        Mockito.when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(addressMapper.toDto(any(Address.class))).thenReturn(newAddressDto);

        // Act
        AddressDto result = addressService.setDefaultAddress(2L, user);

        // Assert
        assertNotNull(result);
        assertTrue(targetAddress.isDefault()); // Target is now default
        assertFalse(existingDefaultAddress.isDefault()); // Old default flipped to false

        Mockito.verify(addressRepository).save(existingDefaultAddress);
        Mockito.verify(addressRepository).save(targetAddress);
    }
}
