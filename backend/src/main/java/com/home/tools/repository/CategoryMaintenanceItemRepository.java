package com.home.tools.repository;

import com.home.tools.entity.CategoryMaintenanceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryMaintenanceItemRepository extends JpaRepository<CategoryMaintenanceItem, Long> {

    List<CategoryMaintenanceItem> findByCategoryId(Long categoryId);

    List<CategoryMaintenanceItem> findByCategoryIdIn(List<Long> categoryIds);

    Optional<CategoryMaintenanceItem> findByCategoryIdAndMaintenanceItemCode(Long categoryId, String maintenanceItemCode);

    void deleteByCategoryId(Long categoryId);
}
