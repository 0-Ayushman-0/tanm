package com.tanm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanm.backend.dto.AddressCreateRequest;
import com.tanm.backend.dto.AddressDto;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.enums.AddressType;
import com.tanm.backend.enums.UserRole;
import com.tanm.backend.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AddressController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = {com.tanm.backend.config.SecurityConfig.class, com.tanm.backend.config.JwtAuthenticationFilter.class}
        )
)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @Autowired
    private ObjectMapper objectMapper;

    private AddressDto addressDto;
    private AddressCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        addressDto = AddressDto.builder()
                .id(1L)
                .fullName("John Doe")
                .phoneNumber("+1234567890")
                .label("Home")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .addressType(AddressType.SHIPPING)
                .isDefault(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = AddressCreateRequest.builder()
                .fullName("John Doe")
                .phoneNumber("+1234567890")
                .label("Home")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .addressType(AddressType.SHIPPING)
                .isDefault(true)
                .build();
    }

    @Test
    void getMyAddresses_shouldReturnList() throws Exception {
        Mockito.when(addressService.getMyAddresses(any()))
                .thenReturn(Collections.singletonList(addressDto));

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$[0].label").value("Home"));
    }

    @Test
    void getAddressById_shouldReturnAddress() throws Exception {
        Mockito.when(addressService.getAddressById(eq(1L), any()))
                .thenReturn(addressDto);

        mockMvc.perform(get("/api/addresses/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void createAddress_shouldReturnCreatedAddress() throws Exception {
        Mockito.when(addressService.createAddress(any(AddressCreateRequest.class), any()))
                .thenReturn(addressDto);

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateAddress_shouldReturnUpdatedAddress() throws Exception {
        Mockito.when(addressService.updateAddress(eq(1L), any(AddressCreateRequest.class), any()))
                .thenReturn(addressDto);

        mockMvc.perform(put("/api/addresses/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deleteAddress_shouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(addressService).deleteAddress(eq(1L), any());

        mockMvc.perform(delete("/api/addresses/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void setDefaultAddress_shouldReturnUpdatedAddress() throws Exception {
        Mockito.when(addressService.setDefaultAddress(eq(1L), any()))
                .thenReturn(addressDto);

        mockMvc.perform(patch("/api/addresses/{id}/default", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.default").value(true));
    }
}
