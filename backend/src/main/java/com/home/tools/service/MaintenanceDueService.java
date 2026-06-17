package com.home.tools.service;

import com.home.tools.dto.MaintenanceDashboardStats;
import com.home.tools.dto.MaintenanceDueResult;
import com.home.tools.entity.MaintenanceDueStatus;
import com.home.tools.entity.Tool;

import java.util.List;

public interface MaintenanceDueService {

    MaintenanceDueResult calculateForTool(Long toolId);

    MaintenanceDueResult calculateForTool(Tool tool);

    List<MaintenanceDueResult> calculateForAllTools();

    List<MaintenanceDueResult> calculateForTools(List<Long> toolIds);

    List<MaintenanceDueResult> calculateByCategory(Long categoryId);

    List<MaintenanceDueResult> findByStatus(MaintenanceDueStatus status);

    List<MaintenanceDueResult> findDueOrOverdue();

    MaintenanceDashboardStats getDashboardStats();

    MaintenanceDueStatus determineStatus(Integer effectiveCycleDays,
                                         java.time.LocalDate lastMaintenanceDate,
                                         java.time.LocalDate purchaseDate,
                                         java.time.LocalDate storedDueDate);
}
