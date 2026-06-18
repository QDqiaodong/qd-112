package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.PageResult;
import com.home.tools.dto.PartReplacementDTO;
import com.home.tools.entity.PartReplacement;
import com.home.tools.service.CacheService;
import com.home.tools.service.PartReplacementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/part-replacements")
public class PartReplacementController {

    private final PartReplacementService partReplacementService;
    private final CacheService cacheService;

    public PartReplacementController(PartReplacementService partReplacementService, CacheService cacheService) {
        this.partReplacementService = partReplacementService;
        this.cacheService = cacheService;
    }

    @GetMapping
    public ApiResponse<PageResult<PartReplacement>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long toolId,
            @RequestParam(required = false) String partType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.ok(partReplacementService.list(page, size, toolId, partType, startDate, endDate));
    }

    @GetMapping("/tool/{toolId}")
    public ApiResponse<List<PartReplacement>> findByToolId(@PathVariable Long toolId) {
        return ApiResponse.ok(partReplacementService.findByToolId(toolId));
    }

    @PostMapping
    public ApiResponse<PartReplacement> create(@RequestBody PartReplacementDTO dto) {
        PartReplacement result = partReplacementService.create(dto);
        cacheService.evictStatsCache();
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<PartReplacement> update(@PathVariable Long id, @RequestBody PartReplacementDTO dto) {
        PartReplacement result = partReplacementService.update(id, dto);
        cacheService.evictStatsCache();
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        partReplacementService.delete(id);
        cacheService.evictStatsCache();
        return ApiResponse.ok(null);
    }
}
