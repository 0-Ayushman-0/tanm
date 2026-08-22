package com.tanm.backend.controller;

import com.tanm.backend.dto.SiteSettingDto;
import com.tanm.backend.service.CmsSiteSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cms/settings")
@RequiredArgsConstructor
public class AdminCmsSiteSettingController {

    private final CmsSiteSettingService siteSettingService;

    @GetMapping
    public ResponseEntity<List<SiteSettingDto>> getAll() {
        return ResponseEntity.ok(siteSettingService.getAllSettingsAdmin());
    }

    @GetMapping("/{key}")
    public ResponseEntity<SiteSettingDto> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(siteSettingService.getSettingByKey(key));
    }

    @PutMapping("/{key}")
    public ResponseEntity<SiteSettingDto> update(@PathVariable String key, @Valid @RequestBody SiteSettingDto dto) {
        return ResponseEntity.ok(siteSettingService.updateSetting(key, dto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Void> updateBulk(@Valid @RequestBody List<SiteSettingDto> settings) {
        siteSettingService.updateBulkSettings(settings);
        return ResponseEntity.ok().build();
    }
}
