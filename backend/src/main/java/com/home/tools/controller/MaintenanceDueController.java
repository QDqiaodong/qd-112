package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.MaintenanceDashboardStats;
import com.home.tools.dto.MaintenanceDueResult;
import com.home.tools.entity.MaintenanceDueStatus;
import com.home.tools.service.MaintenanceDueService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/maintenance-due")
public class MaintenanceDueController {

    private final MaintenanceDueService maintenanceDueService;

    public MaintenanceDueController(MaintenanceDueService maintenanceDueService) {
        this.maintenanceDueService = maintenanceDueService;
    }

    @GetMapping("/tool/{toolId}")
    public ApiResponse<MaintenanceDueResult> getForTool(@PathVariable Long toolId) {
        return ApiResponse.ok(maintenanceDueService.calculateForTool(toolId));
    }

    @GetMapping("/all")
    public ApiResponse<List<MaintenanceDueResult>> getAll() {
        return ApiResponse.ok(maintenanceDueService.calculateForAllTools());
    }

    @PostMapping("/batch")
    public ApiResponse<List<MaintenanceDueResult>> getBatch(@RequestBody List<Long> toolIds) {
        return ApiResponse.ok(maintenanceDueService.calculateForTools(toolIds));
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<MaintenanceDueResult>> getByCategory(@PathVariable Long categoryId) {
        return ApiResponse.ok(maintenanceDueService.calculateByCategory(categoryId));
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<MaintenanceDueResult>> getByStatus(@PathVariable MaintenanceDueStatus status) {
        return ApiResponse.ok(maintenanceDueService.findByStatus(status));
    }

    @GetMapping("/due-or-overdue")
    public ApiResponse<List<MaintenanceDueResult>> getDueOrOverdue() {
        return ApiResponse.ok(maintenanceDueService.findDueOrOverdue());
    }

    @GetMapping("/dashboard")
    public ApiResponse<MaintenanceDashboardStats> getDashboard() {
        return ApiResponse.ok(maintenanceDueService.getDashboardStats());
    }

    @GetMapping("/statuses")
    public ApiResponse<List<Object>> getStatusList() {
        List<Object> statuses = Arrays.stream(MaintenanceDueStatus.values())
                .map(s -> {
                    var map = new java.util.LinkedHashMap<String, Object>();
                    map.put("name", s.name());
                    map.put("description", s.getDescription());
                    map.put("severity", s.getSeverity());
                    map.put("isDueOrOverdue", s.isDueOrOverdue());
                    map.put("isWarning", s.isWarning());
                    return map;
                })
                .collect(Collectors.toList());
        return ApiResponse.ok(statuses);
    }
}
