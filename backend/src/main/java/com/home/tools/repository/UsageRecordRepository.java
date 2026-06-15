package com.home.tools.repository;

import com.home.tools.entity.UsageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    Page<UsageRecord> findByToolId(Long toolId, Pageable pageable);

    List<UsageRecord> findByToolId(Long toolId);

    List<UsageRecord> findByToolIdOrderByUseDateDesc(Long toolId);

    Page<UsageRecord> findByUseDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate, Pageable pageable);

    Page<UsageRecord> findByToolIdAndUseDateBetween(Long toolId, java.time.LocalDate startDate, java.time.LocalDate endDate, Pageable pageable);

    void deleteByToolId(Long toolId);
}
