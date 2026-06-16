package com.home.tools.repository;

import com.home.tools.entity.Tool;
import com.home.tools.entity.ToolStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ToolRepository extends JpaRepository<Tool, Long> {

    Page<Tool> findByCategoryId(Long categoryId, Pageable pageable);

    List<Tool> findByCategoryId(Long categoryId);

    List<Tool> findByStatus(ToolStatus status);

    List<Tool> findByNextMaintenanceDateBefore(LocalDate date);

    List<Tool> findByNextMaintenanceDateBetween(LocalDate startDate, LocalDate endDate);

    Page<Tool> findByNameContainingOrModelContainingOrBrandContaining(String name, String model, String brand, Pageable pageable);

    long countByCategoryId(Long categoryId);

    long countBySubCategoryId(Long subCategoryId);
}
