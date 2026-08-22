package com.tanm.backend.service;

import com.tanm.backend.dto.SiteSettingDto;

import java.util.List;
import java.util.Map;

public interface CmsSiteSettingService {
    Map<String, String> getAllSettingsAsMap();
    List<SiteSettingDto> getAllSettingsAdmin();
    SiteSettingDto getSettingByKey(String key);
    SiteSettingDto updateSetting(String key, SiteSettingDto dto);
    void updateBulkSettings(List<SiteSettingDto> settings);
}
