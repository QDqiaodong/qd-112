package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.CategoryMaintenanceInheritanceResult;
import com.home.tools.dto.MaintenanceItemWithSource;
import com.home.tools.entity.CategoryMaintenanceItem;
import com.home.tools.service.CategoryMaintenanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category-maintenance")
public class CategoryMaintenanceController {

    private final CategoryMaintenanceService categoryMaintenanceService;

    public CategoryMaintenanceController(CategoryMaintenanceService categoryMaintenanceService) {
        this.categoryMaintenanceService = categoryMaintenanceService;
    }

    @GetMapping("/inheritance/{categoryId}")
    public ApiResponse<CategoryMaintenanceInheritanceResult> getInheritance(@PathVariable Long categoryId) {
        return ApiResponse.ok(categoryMaintenanceService.getInheritanceResult(categoryId));
    }

    @GetMapping("/effective/{categoryId}")
    public ApiResponse<List<MaintenanceItemWithSource>> getEffectiveItems(@PathVariable Long categoryId) {
        return ApiResponse.ok(categoryMaintenanceService.getEffectiveItems(categoryId));
    }

    @GetMapping("/tool/{toolId}")
    public ApiResponse<List<MaintenanceItemWithSource>> getEffectiveItemsForTool(@PathVariable Long toolId) {
        return ApiResponse.ok(categoryMaintenanceService.getEffectiveItemsForTool(toolId));
    }

    @GetMapping("/items/{categoryId}")
    public ApiResponse<List<CategoryMaintenanceItem>> getCategoryItems(@PathVariable Long categoryId) {
        return ApiResponse.ok(categoryMaintenanceService.getCategoryItems(categoryId));
    }

    @PostMapping("/items")
    public ApiResponse<CategoryMaintenanceItem> saveItem(@RequestBody CategoryMaintenanceItem item) {
        return ApiResponse.ok(categoryMaintenanceService.saveCategoryItem(item));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        categoryMaintenanceService.deleteCategoryItem(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/items/batch/{categoryId}")
    public ApiResponse<Void> batchSaveItems(@PathVariable Long categoryId,
                                            @RequestBody List<CategoryMaintenanceItem> items) {
        categoryMaintenanceService.batchSaveCategoryItems(categoryId, items);
        return ApiResponse.ok(null);
    }

    @PostMapping("/inherit/{categoryId}")
    public ApiResponse<Void> inheritFromParent(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "false") boolean overrideExisting) {
        categoryMaintenanceService.inheritFromParent(categoryId, overrideExisting);
        return ApiResponse.ok(null);
    }
}
