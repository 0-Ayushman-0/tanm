package com.tanm.backend.repository;

import com.tanm.backend.entity.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteSettingRepository extends JpaRepository<SiteSetting, Long> {
    Optional<SiteSetting> findByKeyAndIsDeletedFalse(String key);
    List<SiteSetting> findBySettingGroupAndIsDeletedFalse(String settingGroup);
    List<SiteSetting> findByIsDeletedFalse();
}
