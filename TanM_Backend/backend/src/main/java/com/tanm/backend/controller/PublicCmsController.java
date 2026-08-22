package com.tanm.backend.controller;

import com.tanm.backend.dto.*;
import com.tanm.backend.enums.BannerType;
import com.tanm.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cms")
@RequiredArgsConstructor
public class PublicCmsController {

    private final CmsHydrationService hydrationService;
    private final CmsPageService pageService;
    private final CmsFaqService faqService;
    private final CmsTestimonialService testimonialService;
    private final CmsBannerService bannerService;

    @GetMapping("/hydration")
    public ResponseEntity<CmsHydrationDto> getHydrationPayload() {
        return ResponseEntity.ok(hydrationService.getHydrationPayload());
    }

    @GetMapping("/pages/{slug}")
    public ResponseEntity<CmsStaticPageDto> getPublishedPage(@PathVariable String slug) {
        return ResponseEntity.ok(pageService.getPublishedPageBySlug(slug));
    }

    @GetMapping("/faqs")
    public ResponseEntity<List<CmsFaqDto>> getPublishedFaqs() {
        return ResponseEntity.ok(faqService.getPublishedFaqs());
    }

    @GetMapping("/testimonials")
    public ResponseEntity<List<CmsTestimonialDto>> getPublishedTestimonials() {
        return ResponseEntity.ok(testimonialService.getPublishedTestimonials());
    }

    @GetMapping("/banners/{type}")
    public ResponseEntity<List<CmsBannerDto>> getActiveBanners(@PathVariable BannerType type) {
        return ResponseEntity.ok(bannerService.getActiveBannersByType(type));
    }
}
