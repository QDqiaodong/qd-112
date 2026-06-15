package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.MaintenanceTrackDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.dto.ToolDTO;
import com.home.tools.entity.Tool;
import com.home.tools.service.ToolService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @GetMapping
    public ApiResponse<PageResult<Tool>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(toolService.list(page, size, keyword, categoryId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Tool> getById(@PathVariable Long id) {
        return ApiResponse.ok(toolService.getById(id));
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
}
