package com.tanm.backend.repository;

import com.tanm.backend.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findByStorageKeyAndIsDeletedFalse(String storageKey);
    Page<Media> findByIsDeletedFalse(Pageable pageable);
    Page<Media> findByFolderAndIsDeletedFalse(String folder, Pageable pageable);
}
