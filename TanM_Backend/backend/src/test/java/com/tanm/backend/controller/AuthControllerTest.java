package com.tanm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanm.backend.dto.*;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.enums.UserRole;
import com.tanm.backend.mapper.UserMapper;
import com.tanm.backend.service.AuthService;
import com.tanm.backend.service.PasswordResetService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = {com.tanm.backend.config.SecurityConfig.class, com.tanm.backend.config.JwtAuthenticationFilter.class}
        )
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto userDto;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        registerRequest = RegisterRequest.builder()
                .email("user@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .email("user@example.com")
                .password("password123")
                .build();

        authResponse = AuthResponse.builder()
                .token("jwt-token-abc")
                .type("Bearer")
                .user(userDto)
                .build();
    }

    @Test
    void register_shouldReturnCreatedUser() throws Exception {
        Mockito.when(authService.register(any(RegisterRequest.class)))
                .thenReturn(userDto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void verifyEmail_shouldReturnSuccessMessage() throws Exception {
        VerifyEmailRequest verifyRequest = new VerifyEmailRequest();
        verifyRequest.setEmail("user@example.com");
        verifyRequest.setOtpCode("123456");

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully. Your account is now active."));

        Mockito.verify(authService).verifyEmail(eq("user@example.com"), eq("123456"));
    }

    @Test
    void login_shouldReturnAuthResponse() throws Exception {
        Mockito.when(authService.login(any(LoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-abc"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("user@example.com"));
    }

    @Test
    void requestPasswordReset_shouldReturn200() throws Exception {
        PasswordResetRequestDto dto = new PasswordResetRequestDto();
        dto.setEmail("user@example.com");

        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        Mockito.verify(passwordResetService).requestPasswordReset(eq("user@example.com"));
    }

    @Test
    void confirmPasswordReset_shouldReturn200() throws Exception {
        PasswordResetConfirmDto dto = new PasswordResetConfirmDto();
        dto.setEmail("user@example.com");
        dto.setOtpCode("123456");
        dto.setNewPassword("newpassword123");

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        Mockito.verify(passwordResetService).confirmPasswordReset(
                eq("user@example.com"), eq("123456"), eq("newpassword123"));
    }

    @Test
    void getMe_shouldReturnUserDto() throws Exception {
        AppUser principal = AppUser.builder()
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .build();

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        java.util.Collections.emptyList()
                );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(userMapper.toDto(any(AppUser.class)))
                .thenReturn(userDto);

        try {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("user@example.com"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
