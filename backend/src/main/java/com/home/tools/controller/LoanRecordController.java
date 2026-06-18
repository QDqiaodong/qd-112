package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.LoanRecordDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.LoanRecord;
import com.home.tools.entity.LoanStatus;
import com.home.tools.service.CacheService;
import com.home.tools.service.LoanRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/loan-records")
public class LoanRecordController {

    private final LoanRecordService loanRecordService;
    private final CacheService cacheService;

    public LoanRecordController(LoanRecordService loanRecordService, CacheService cacheService) {
        this.loanRecordService = loanRecordService;
        this.cacheService = cacheService;
    }

    @GetMapping
    public ApiResponse<PageResult<LoanRecord>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long toolId,
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.ok(loanRecordService.list(page, size, toolId, status, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ApiResponse<LoanRecord> getById(@PathVariable Long id) {
        return ApiResponse.ok(loanRecordService.getById(id));
    }

    @GetMapping("/tool/{toolId}")
    public ApiResponse<List<LoanRecord>> findByToolId(@PathVariable Long toolId) {
        return ApiResponse.ok(loanRecordService.findByToolId(toolId));
    }

    @PostMapping
    public ApiResponse<LoanRecord> create(@RequestBody LoanRecordDTO dto) {
        try {
            LoanRecord result = loanRecordService.create(dto);
            cacheService.evictStatsCache();
            return ApiResponse.ok(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<LoanRecord> update(@PathVariable Long id, @RequestBody LoanRecordDTO dto) {
        try {
            LoanRecord result = loanRecordService.update(id, dto);
            cacheService.evictStatsCache();
            return ApiResponse.ok(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/return")
    public ApiResponse<LoanRecord> returnTool(@PathVariable Long id, @RequestBody LoanRecordDTO dto) {
        try {
            LoanRecord result = loanRecordService.returnTool(id, dto);
            cacheService.evictStatsCache();
            return ApiResponse.ok(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            loanRecordService.delete(id);
            cacheService.evictStatsCache();
            return ApiResponse.ok(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
