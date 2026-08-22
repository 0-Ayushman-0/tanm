package com.tanm.backend.controller;

import com.tanm.backend.dto.OrderDto;
import com.tanm.backend.dto.OrderItemDto;
import com.tanm.backend.enums.FulfillmentStatus;
import com.tanm.backend.enums.PaymentMethod;
import com.tanm.backend.enums.PaymentStatus;
import com.tanm.backend.service.EmailService;
import com.tanm.backend.entity.AppUser;
import com.tanm.backend.enums.UserRole;
import com.tanm.backend.repository.AppUserRepository;
import com.tanm.backend.repository.CmsHeroSlideRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DEV-ONLY controller for sending test emails to verify all templates.
 * Only active when spring.profiles.active=dev.
 * Remove or restrict before going to production.
 */
@RestController
@RequestMapping("/api/dev/email-test")
@RequiredArgsConstructor
@Profile("dev")
public class DevEmailTestController {

    private final EmailService emailService;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CmsHeroSlideRepository cmsHeroSlideRepository;
    private final com.tanm.backend.repository.ProductRepository productRepository;
    private final com.tanm.backend.repository.ProductImageRepository productImageRepository;

    @PostMapping("/clear-slides")
    public ResponseEntity<Map<String, String>> clearSlides() {
        cmsHeroSlideRepository.deleteAll();
        return ok("All hero slides have been deleted from the database");
    }

    @PostMapping("/update-mock-images")
    public ResponseEntity<Map<String, String>> updateMockImages() {
        try {
            // Product 6: Duffle Bag
            productRepository.findById(6L).ifPresent(p -> {
                p.setFeatured(true);
                p.setLeatherType("Full Grain Vachetta");
                p.setColor("Cognac");
                p.setDimensions("18\"W x 10\"H x 9\"D");
                productRepository.save(p);

                // Delete existing images then add primary image directly via its own repository
                productImageRepository.deleteAll(productImageRepository.findAll()
                    .stream().filter(img -> img.getProduct().getId().equals(6L))
                    .collect(java.util.stream.Collectors.toList()));

                com.tanm.backend.entity.ProductImage img = com.tanm.backend.entity.ProductImage.builder()
                    .imageUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuC2BwfhfxhcodMsa6y1VaVz86Tgetrc_slQtjNzAZ85ET0v245izXZ0EtNNTK0f5lGM-tlwMFWtQreA5Z_mc-2Y4qQxcmLSKEsj70Kv-fljvFX4-zWwYP3WD2cAVvzpyMhcGugdrHZArrGmjKozuW9v_ISJS8Jz77EdbXa1SphXXU-2ZmkCJZLMkw6BBDa8iMfqRmS6T2kT0BRvyhaEHVCZyZ7-AxWf7mFJVxInd8PC8XSHXnoZETksMzZ-ifusZc2w-H6zlpjieNtM")
                    .isPrimary(true)
                    .product(productRepository.findById(6L).get())
                    .build();
                productImageRepository.save(img);
            });

            // Product 5: Cardholder
            productRepository.findById(5L).ifPresent(p -> {
                p.setFeatured(true);
                p.setLeatherType("Pebbled Calfskin");
                p.setColor("Black");
                p.setDimensions("4\"W x 3\"H x 0.2\"D");
                productRepository.save(p);

                productImageRepository.deleteAll(productImageRepository.findAll()
                    .stream().filter(img -> img.getProduct().getId().equals(5L))
                    .collect(java.util.stream.Collectors.toList()));

                com.tanm.backend.entity.ProductImage img = com.tanm.backend.entity.ProductImage.builder()
                    .imageUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuDfOkcMcJSBASvbybSj94Q7KTK58m5O3Py-28m9fvVqB8JvrYE_fbbxIjlALX9m1Q2ukdE0JwHgBdyiqhoiBSIWAdgfdbd_O1WtAvLXP5qT69_qZrlcAj6Fy1gHGi-DUdgMh_oOj0-kcuzKHhB0tO-jV1ilFshFIi4nJ2iCbuo7v7SD-aeN2bzjB4FhoEoqPHReIV6IokjqRxjKwQUM0LGb7qFtrAVmu4b2DprCLcfTiAz8cAS1kVtSHSgxpX9Zla36GhiqBa4Dvlhc")
                    .isPrimary(true)
                    .product(productRepository.findById(5L).get())
                    .build();
                productImageRepository.save(img);
            });

            return ok("Mock product images and details seeded successfully");
        } catch (Throwable t) {
            Throwable root = t;
            while (root.getCause() != null) root = root.getCause();
            return ResponseEntity.status(500).body(Map.of("error", root.toString(), "message", String.valueOf(root.getMessage())));
        }
    }

    private static final String TEST_EMAIL = "albusdumbledoor004@gmail.com";

    @PostMapping("/welcome")
    public ResponseEntity<Map<String, String>> testWelcome() {
        emailService.sendWelcomeEmail(TEST_EMAIL, "Aditya");
        return ok("Welcome email sent");
    }

    @PostMapping("/login-otp")
    public ResponseEntity<Map<String, String>> testLoginOtp() {
        emailService.sendLoginOtp(TEST_EMAIL, "482910");
        return ok("Login OTP email sent");
    }

    @PostMapping("/password-reset-otp")
    public ResponseEntity<Map<String, String>> testPasswordResetOtp() {
        emailService.sendPasswordResetOtp(TEST_EMAIL, "739421");
        return ok("Password Reset OTP email sent");
    }

    @PostMapping("/password-changed")
    public ResponseEntity<Map<String, String>> testPasswordChanged() {
        emailService.sendPasswordChangedNotification(TEST_EMAIL, "Aditya");
        return ok("Password Changed Notification email sent");
    }

    @PostMapping("/order-confirmation")
    public ResponseEntity<Map<String, String>> testOrderConfirmation() {
        OrderDto order = buildDummyOrder();
        emailService.sendOrderConfirmation(order);
        return ok("Order Confirmation email sent");
    }

    @PostMapping("/shipping-update")
    public ResponseEntity<Map<String, String>> testShippingUpdate() {
        OrderDto order = buildDummyOrder();
        order.setTrackingNumber("TRK-98765432");
        order.setCarrier("BlueDart");
        order.setFulfillmentStatus(FulfillmentStatus.SHIPPED);
        emailService.sendShippingUpdate(order);
        return ok("Shipping Update email sent");
    }

    @PostMapping("/email-verification-otp")
    public ResponseEntity<Map<String, String>> testEmailVerificationOtp() {
        emailService.sendEmailVerificationOtp(TEST_EMAIL, "582910");
        return ok("Email verification OTP sent");
    }

    @PostMapping("/all")
    public ResponseEntity<Map<String, String>> testAll() {
        emailService.sendWelcomeEmail(TEST_EMAIL, "Aditya");
        emailService.sendLoginOtp(TEST_EMAIL, "482910");
        emailService.sendPasswordResetOtp(TEST_EMAIL, "739421");
        emailService.sendEmailVerificationOtp(TEST_EMAIL, "582910");
        emailService.sendPasswordChangedNotification(TEST_EMAIL, "Aditya");

        OrderDto order = buildDummyOrder();
        emailService.sendOrderConfirmation(order);

        order.setTrackingNumber("TRK-98765432");
        order.setCarrier("BlueDart");
        order.setFulfillmentStatus(FulfillmentStatus.SHIPPED);
        emailService.sendShippingUpdate(order);

        return ok("All test emails sent to " + TEST_EMAIL);
    }

    @PostMapping("/seed-admin")
    public ResponseEntity<Map<String, String>> seedAdmin() {
        if (appUserRepository.existsByEmailAndIsDeletedFalse("admin@tanm.com")) {
            return ok("Admin account already exists");
        }
        AppUser admin = AppUser.builder()
                .firstName("Julian")
                .lastName("Thorne")
                .email("admin@tanm.com")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .isEmailVerified(true)
                .build();
        admin.setDeleted(false);
        appUserRepository.save(admin);
        return ok("Admin account seeded successfully: admin@tanm.com / admin123");
    }

    // ========================================================
    // Helpers
    // ========================================================

    private OrderDto buildDummyOrder() {
        OrderItemDto item1 = OrderItemDto.builder()
                .productName("The Minimalist Wallet")
                .sku("WLT-MIN-001")
                .color("Tan")
                .leatherType("Full-Grain")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(1499.00))
                .subtotal(BigDecimal.valueOf(1499.00))
                .build();

        OrderItemDto item2 = OrderItemDto.builder()
                .productName("Bifold Card Holder")
                .sku("CRD-BFD-002")
                .color("Dark Brown")
                .leatherType("Top-Grain")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(799.00))
                .subtotal(BigDecimal.valueOf(1598.00))
                .build();

        return OrderDto.builder()
                .orderNumber("TNM-2026-DEMO001")
                .userEmail(TEST_EMAIL)
                .paymentStatus(PaymentStatus.PAID)
                .fulfillmentStatus(FulfillmentStatus.CONFIRMED)
                .paymentMethod(PaymentMethod.RAZORPAY)
                .shippingFullName("Aditya Mishra")
                .shippingAddressLine1("12B, Tech Park Lane")
                .shippingCity("Bangalore")
                .shippingState("Karnataka")
                .shippingPostalCode("560001")
                .shippingCountry("India")
                .shippingPhoneNumber("+91 98765 43210")
                .billingFullName("Aditya Mishra")
                .billingAddressLine1("12B, Tech Park Lane")
                .billingCity("Bangalore")
                .billingState("Karnataka")
                .billingPostalCode("560001")
                .billingCountry("India")
                .subtotal(BigDecimal.valueOf(3097.00))
                .shippingFee(BigDecimal.valueOf(99.00))
                .taxFee(BigDecimal.valueOf(55.75))
                .grandTotal(BigDecimal.valueOf(3251.75))
                .items(List.of(item1, item2))
                .build();
    }

    private ResponseEntity<Map<String, String>> ok(String message) {
        return ResponseEntity.ok(Map.of("message", message));
    }
}
