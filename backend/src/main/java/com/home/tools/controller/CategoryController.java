package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.entity.MaintenanceItem;
import com.home.tools.service.CategoryService;
import com.home.tools.service.CacheService;
import com.home.tools.service.ToolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;
    private final CacheService cacheService;
    private final ToolService toolService;
    private final ObjectMapper objectMapper;

    public CategoryController(CategoryService categoryService,
                              CacheService cacheService,
                              ToolService toolService,
                              ObjectMapper objectMapper) {
        this.categoryService = categoryService;
        this.cacheService = cacheService;
        this.toolService = toolService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/categories/tree")
    public ApiResponse<List<Map<String, Object>>> getCategoryTree() {
        return ApiResponse.ok(categoryService.getCategoryTree());
    }

    @GetMapping("/maintenance-items")
    public ApiResponse<List<MaintenanceItem>> listMaintenanceItems() {
        return ApiResponse.ok(categoryService.listMaintenanceItems());
    }

    @GetMapping("/stats/overview")
    public ApiResponse<Map<String, Object>> getStatsOverview() {
        String cached = cacheService.getStatsOverviewFromCache();
        if (cached != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = objectMapper.readValue(cached, Map.class);
                return ApiResponse.ok(data);
            } catch (Exception ignored) {
            }
        }

        Map<String, Object> stats = new HashMap<>();
        Map<String, Long> statusCounts = toolService.countByStatus();
        stats.put("statusCounts", statusCounts);
        stats.put("dueMaintenanceCount", (long) toolService.findDueMaintenance().size());

        try {
            cacheService.cacheStatsOverview(objectMapper.writeValueAsString(stats));
        } catch (Exception ignored) {
        }

        return ApiResponse.ok(stats);
    }
}
