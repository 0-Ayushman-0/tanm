package com.tanm.backend.controller;

import com.tanm.backend.dto.AddressCreateRequest;
import com.tanm.backend.dto.AddressDto;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressDto>> getMyAddresses(
            @RequestHeader(value = "Guest-Token", required = false) String guestToken
    ) {
        AppUser user = getAuthenticatedUser();
        return ResponseEntity.ok(addressService.getMyAddresses(user, guestToken));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDto> getAddressById(
            @PathVariable Long id,
            @RequestHeader(value = "Guest-Token", required = false) String guestToken
    ) {
        AppUser user = getAuthenticatedUser();
        return ResponseEntity.ok(addressService.getAddressById(id, user, guestToken));
    }

    @PostMapping
    public ResponseEntity<AddressDto> createAddress(
            @Valid @RequestBody AddressCreateRequest request,
            @RequestHeader(value = "Guest-Token", required = false) String guestToken
    ) {
        AppUser user = getAuthenticatedUser();
        if (user == null && (guestToken == null || guestToken.isBlank())) {
            guestToken = java.util.UUID.randomUUID().toString();
        }
        AddressDto created = addressService.createAddress(request, user, guestToken);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressCreateRequest request,
            @RequestHeader(value = "Guest-Token", required = false) String guestToken
    ) {
        AppUser user = getAuthenticatedUser();
        AddressDto updated = addressService.updateAddress(id, request, user, guestToken);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @RequestHeader(value = "Guest-Token", required = false) String guestToken
    ) {
        AppUser user = getAuthenticatedUser();
        addressService.deleteAddress(id, user, guestToken);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<AddressDto> setDefaultAddress(
            @PathVariable Long id,
            @RequestHeader(value = "Guest-Token", required = false) String guestToken
    ) {
        AppUser user = getAuthenticatedUser();
        AddressDto updated = addressService.setDefaultAddress(id, user, guestToken);
        return ResponseEntity.ok(updated);
    }

    private AppUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUser) {
            return (AppUser) authentication.getPrincipal();
        }
        return null;
    }
}
