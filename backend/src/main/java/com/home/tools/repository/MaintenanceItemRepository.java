package com.home.tools.repository;

import com.home.tools.entity.MaintenanceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaintenanceItemRepository extends JpaRepository<MaintenanceItem, Long> {
    Optional<MaintenanceItem> findByCode(String code);
}
