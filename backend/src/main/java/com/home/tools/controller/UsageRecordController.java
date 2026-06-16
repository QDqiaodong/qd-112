package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.PageResult;
import com.home.tools.dto.ScenarioAnalysisDTO;
import com.home.tools.dto.UsageRecordDTO;
import com.home.tools.entity.UsageRecord;
import com.home.tools.service.UsageRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/usage")
public class UsageRecordController {

    private final UsageRecordService usageRecordService;

    public UsageRecordController(UsageRecordService usageRecordService) {
        this.usageRecordService = usageRecordService;
    }

    @GetMapping
    public ApiResponse<PageResult<UsageRecord>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long toolId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.ok(usageRecordService.list(page, size, toolId, startDate, endDate));
    }

    @GetMapping("/scenario-analysis")
    public ApiResponse<List<ScenarioAnalysisDTO>> getScenarioAnalysis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.ok(usageRecordService.getScenarioAnalysis(startDate, endDate));
    }

    @GetMapping("/tool/{toolId}")
    public ApiResponse<List<UsageRecord>> findByToolId(@PathVariable Long toolId) {
        return ApiResponse.ok(usageRecordService.findByToolId(toolId));
    }

    @PostMapping
    public ApiResponse<UsageRecord> create(@RequestBody UsageRecordDTO dto) {
        return ApiResponse.ok(usageRecordService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<UsageRecord> update(@PathVariable Long id, @RequestBody UsageRecordDTO dto) {
        return ApiResponse.ok(usageRecordService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        usageRecordService.delete(id);
        return ApiResponse.ok(null);
    }
}
