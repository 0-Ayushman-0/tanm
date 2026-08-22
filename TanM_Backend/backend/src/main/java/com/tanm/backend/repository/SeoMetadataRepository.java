package com.tanm.backend.repository;

import com.tanm.backend.entity.SeoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeoMetadataRepository extends JpaRepository<SeoMetadata, Long> {
}
