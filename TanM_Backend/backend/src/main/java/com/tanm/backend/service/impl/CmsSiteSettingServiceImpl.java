package com.tanm.backend.service.impl;

import com.tanm.backend.dto.SiteSettingDto;
import com.tanm.backend.entity.SiteSetting;
import com.tanm.backend.exception.ResourceNotFoundException;
import com.tanm.backend.repository.SiteSettingRepository;
import com.tanm.backend.service.CmsSiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmsSiteSettingServiceImpl implements CmsSiteSettingService {

    private final SiteSettingRepository siteSettingRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getAllSettingsAsMap() {
        List<SiteSetting> list = siteSettingRepository.findByIsDeletedFalse();
        Map<String, String> map = new HashMap<>();
        for (SiteSetting s : list) {
            map.put(s.getKey(), s.getValue());
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteSettingDto> getAllSettingsAdmin() {
        return siteSettingRepository.findByIsDeletedFalse().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SiteSettingDto getSettingByKey(String key) {
        SiteSetting setting = siteSettingRepository.findByKeyAndIsDeletedFalse(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found for key: " + key));
        return toDto(setting);
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public SiteSettingDto updateSetting(String key, SiteSettingDto dto) {
        SiteSetting setting = siteSettingRepository.findByKeyAndIsDeletedFalse(key)
                .orElseGet(() -> SiteSetting.builder()
                        .key(key)
                        .settingGroup(dto.getSettingGroup() != null ? dto.getSettingGroup() : "GENERAL")
                        .valueType(dto.getValueType() != null ? dto.getValueType() : "STRING")
                        .build());

        setting.setValue(dto.getValue());
        if (dto.getSettingGroup() != null) setting.setSettingGroup(dto.getSettingGroup());
        if (dto.getValueType() != null) setting.setValueType(dto.getValueType());

        return toDto(siteSettingRepository.save(setting));
    }

    @Override
    @Transactional
    @CacheEvict(value = "publicCmsHydration", allEntries = true)
    public void updateBulkSettings(List<SiteSettingDto> settings) {
        for (SiteSettingDto dto : settings) {
            updateSetting(dto.getKey(), dto);
        }
    }

    public SiteSettingDto toDto(SiteSetting setting) {
        if (setting == null) return null;
        return SiteSettingDto.builder()
                .id(setting.getId())
                .key(setting.getKey())
                .value(setting.getValue())
                .settingGroup(setting.getSettingGroup())
                .valueType(setting.getValueType())
                .build();
    }
}
