package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.MaintenanceRecordDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.MaintenanceRecord;
import com.home.tools.entity.Tool;
import com.home.tools.service.CacheService;
import com.home.tools.service.MaintenanceRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceRecordController {

    private final MaintenanceRecordService maintenanceRecordService;
    private final CacheService cacheService;

    public MaintenanceRecordController(MaintenanceRecordService maintenanceRecordService,
                                       CacheService cacheService) {
        this.maintenanceRecordService = maintenanceRecordService;
        this.cacheService = cacheService;
    }

    @GetMapping
    public ApiResponse<PageResult<MaintenanceRecord>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long toolId) {
        return ApiResponse.ok(maintenanceRecordService.list(page, size, toolId));
    }

    @GetMapping("/tool/{toolId}")
    public ApiResponse<List<MaintenanceRecord>> findByToolId(@PathVariable Long toolId) {
        return ApiResponse.ok(maintenanceRecordService.findByToolId(toolId));
    }

    @GetMapping("/due")
    public ApiResponse<List<Tool>> findDueMaintenance() {
        return ApiResponse.ok(maintenanceRecordService.findDueMaintenance());
    }

    @PostMapping
    public ApiResponse<MaintenanceRecord> create(@RequestBody MaintenanceRecordDTO dto) {
        MaintenanceRecord result = maintenanceRecordService.create(dto);
        cacheService.evictStatsCache();
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<MaintenanceRecord> update(@PathVariable Long id, @RequestBody MaintenanceRecordDTO dto) {
        MaintenanceRecord result = maintenanceRecordService.update(id, dto);
        cacheService.evictStatsCache();
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        maintenanceRecordService.delete(id);
        cacheService.evictStatsCache();
        return ApiResponse.ok(null);
    }
}
