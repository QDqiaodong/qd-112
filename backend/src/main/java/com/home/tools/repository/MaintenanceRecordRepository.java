package com.home.tools.repository;

import com.home.tools.entity.MaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    Page<MaintenanceRecord> findByToolId(Long toolId, Pageable pageable);

    List<MaintenanceRecord> findByToolId(Long toolId);
}
