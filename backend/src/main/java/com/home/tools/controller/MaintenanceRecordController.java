package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.MaintenanceRecordDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.MaintenanceRecord;
import com.home.tools.service.MaintenanceRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceRecordController {

    private final MaintenanceRecordService maintenanceRecordService;

    public MaintenanceRecordController(MaintenanceRecordService maintenanceRecordService) {
        this.maintenanceRecordService = maintenanceRecordService;
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
    public ApiResponse<List<MaintenanceRecord>> findDueMaintenance() {
        return ApiResponse.ok(maintenanceRecordService.findDueMaintenance());
    }

    @PostMapping
    public ApiResponse<MaintenanceRecord> create(@RequestBody MaintenanceRecordDTO dto) {
        return ApiResponse.ok(maintenanceRecordService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<MaintenanceRecord> update(@PathVariable Long id, @RequestBody MaintenanceRecordDTO dto) {
        return ApiResponse.ok(maintenanceRecordService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        maintenanceRecordService.delete(id);
        return ApiResponse.ok(null);
    }
}
