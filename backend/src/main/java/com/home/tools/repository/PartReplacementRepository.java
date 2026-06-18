package com.home.tools.repository;

import com.home.tools.entity.PartReplacement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PartReplacementRepository extends JpaRepository<PartReplacement, Long> {

    Page<PartReplacement> findByToolId(Long toolId, Pageable pageable);

    List<PartReplacement> findByToolIdOrderByReplacementDateDesc(Long toolId);

    Page<PartReplacement> findByReplacementDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<PartReplacement> findByToolIdAndReplacementDateBetween(Long toolId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<PartReplacement> findByPartType(String partType, Pageable pageable);

    Page<PartReplacement> findByToolIdAndPartType(Long toolId, String partType, Pageable pageable);

    void deleteByToolId(Long toolId);

    Long countByToolId(Long toolId);
}
