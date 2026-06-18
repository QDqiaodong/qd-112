package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.ToolKitDTO;
import com.home.tools.dto.ToolKitItemDTO;
import com.home.tools.dto.ToolKitWithItemsDTO;
import com.home.tools.entity.ToolKit;
import com.home.tools.service.CacheService;
import com.home.tools.service.ToolKitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tool-kits")
public class ToolKitController {

    private final ToolKitService toolKitService;
    private final CacheService cacheService;

    public ToolKitController(ToolKitService toolKitService, CacheService cacheService) {
        this.toolKitService = toolKitService;
        this.cacheService = cacheService;
    }

    @GetMapping
    public ApiResponse<List<ToolKit>> list() {
        return ApiResponse.ok(toolKitService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<ToolKit> getById(@PathVariable Long id) {
        return ApiResponse.ok(toolKitService.getById(id));
    }

    @GetMapping("/{id}/with-items")
    public ApiResponse<ToolKitWithItemsDTO> getWithItemsById(@PathVariable Long id) {
        return ApiResponse.ok(toolKitService.getWithItemsById(id));
    }

    @PostMapping
    public ApiResponse<ToolKit> create(@RequestBody ToolKitDTO dto) {
        ToolKit result = toolKitService.create(dto);
        cacheService.evictStatsCache();
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<ToolKit> update(@PathVariable Long id, @RequestBody ToolKitDTO dto) {
        try {
            ToolKit result = toolKitService.update(id, dto);
            cacheService.evictStatsCache();
            return ApiResponse.ok(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        toolKitService.delete(id);
        cacheService.evictStatsCache();
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/items")
    public ApiResponse<Void> addItem(@PathVariable Long id, @RequestBody ToolKitItemDTO dto) {
        try {
            toolKitService.addItem(id, dto);
            cacheService.evictStatsCache();
            return ApiResponse.ok(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/items/{toolId}")
    public ApiResponse<Void> removeItem(@PathVariable Long id, @PathVariable Long toolId) {
        toolKitService.removeItem(id, toolId);
        cacheService.evictStatsCache();
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/items")
    public ApiResponse<Void> updateItem(@PathVariable Long id, @RequestBody ToolKitItemDTO dto) {
        try {
            toolKitService.updateItem(id, dto);
            cacheService.evictStatsCache();
            return ApiResponse.ok(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
