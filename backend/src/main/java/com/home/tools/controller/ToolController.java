package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.MaintenanceTrackDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.dto.ToolAvailabilityScore;
import com.home.tools.dto.ToolDTO;
import com.home.tools.dto.ToolWithScore;
import com.home.tools.entity.MaintenanceRecord;
import com.home.tools.entity.Tool;
import com.home.tools.entity.UsageRecord;
import com.home.tools.service.MaintenanceRecordService;
import com.home.tools.service.ToolService;
import com.home.tools.service.UsageRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private final ToolService toolService;
    private final MaintenanceRecordService maintenanceRecordService;
    private final UsageRecordService usageRecordService;

    public ToolController(ToolService toolService,
                          MaintenanceRecordService maintenanceRecordService,
                          UsageRecordService usageRecordService) {
        this.toolService = toolService;
        this.maintenanceRecordService = maintenanceRecordService;
        this.usageRecordService = usageRecordService;
    }

    @GetMapping
    public ApiResponse<PageResult<Tool>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(toolService.list(page, size, keyword, categoryId));
    }

    @GetMapping("/with-score")
    public ApiResponse<PageResult<ToolWithScore>> listWithScore(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(toolService.listWithScore(page, size, keyword, categoryId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Tool> getById(@PathVariable Long id) {
        return ApiResponse.ok(toolService.getById(id));
    }

    @GetMapping("/{id}/availability-score")
    public ApiResponse<ToolAvailabilityScore> getAvailabilityScore(@PathVariable Long id) {
        return ApiResponse.ok(toolService.calculateAvailabilityScore(id));
    }

    @PostMapping
    public ApiResponse<Tool> create(@RequestBody ToolDTO dto) {
        return ApiResponse.ok(toolService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<Tool> update(@PathVariable Long id, @RequestBody ToolDTO dto) {
        return ApiResponse.ok(toolService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        toolService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/due-maintenance")
    public ApiResponse<List<Tool>> findDueMaintenance() {
        return ApiResponse.ok(toolService.findDueMaintenance());
    }

    @GetMapping("/maintenance-by-month")
    public ApiResponse<List<Tool>> findMaintenanceByMonth(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return ApiResponse.ok(toolService.findMaintenanceByMonth(year, month));
    }

    @GetMapping("/{id}/maintenance-track")
    public ApiResponse<List<MaintenanceTrackDTO>> getMaintenanceTrack(@PathVariable Long id) {
        return ApiResponse.ok(toolService.getMaintenanceTrack(id));
    }

    @GetMapping("/{id}/maintenance")
    public ApiResponse<PageResult<MaintenanceRecord>> getMaintenanceByTool(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.ok(maintenanceRecordService.list(page, size, id));
    }

    @GetMapping("/{id}/usage")
    public ApiResponse<PageResult<UsageRecord>> getUsageByTool(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.ok(usageRecordService.list(page, size, id, null, null));
    }
}
