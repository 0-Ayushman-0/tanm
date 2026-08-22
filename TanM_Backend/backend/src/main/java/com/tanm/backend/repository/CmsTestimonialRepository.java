package com.tanm.backend.repository;

import com.tanm.backend.entity.CmsTestimonial;
import com.tanm.backend.enums.CmsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsTestimonialRepository extends JpaRepository<CmsTestimonial, Long> {
    List<CmsTestimonial> findByStatusAndIsDeletedFalseOrderByDisplayOrderAsc(CmsStatus status);
    Page<CmsTestimonial> findByIsDeletedFalse(Pageable pageable);
}
