package com.home.tools.service;

import com.home.tools.dto.CategoryMaintenanceInheritanceResult;
import com.home.tools.dto.MaintenanceItemWithSource;
import com.home.tools.entity.CategoryMaintenanceItem;

import java.util.List;

public interface CategoryMaintenanceService {

    CategoryMaintenanceInheritanceResult getInheritanceResult(Long categoryId);

    List<MaintenanceItemWithSource> getEffectiveItems(Long categoryId);

    List<MaintenanceItemWithSource> getEffectiveItemsForTool(Long toolId);

    CategoryMaintenanceItem saveCategoryItem(CategoryMaintenanceItem item);

    void deleteCategoryItem(Long id);

    List<CategoryMaintenanceItem> getCategoryItems(Long categoryId);

    void batchSaveCategoryItems(Long categoryId, List<CategoryMaintenanceItem> items);

    void inheritFromParent(Long categoryId, boolean overrideExisting);
}
