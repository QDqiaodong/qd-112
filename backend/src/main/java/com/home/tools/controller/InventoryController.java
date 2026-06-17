package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.DifferenceGroupDTO;
import com.home.tools.dto.InventoryCompletionResultDTO;
import com.home.tools.dto.InventoryDTO;
import com.home.tools.dto.InventoryItemDTO;
import com.home.tools.dto.InventoryProgressDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.Inventory;
import com.home.tools.entity.InventoryItem;
import com.home.tools.repository.InventoryItemRepository;
import com.home.tools.service.CacheService;
import com.home.tools.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryItemRepository inventoryItemRepository;
    private final CacheService cacheService;

    public InventoryController(InventoryService inventoryService,
                               InventoryItemRepository inventoryItemRepository,
                               CacheService cacheService) {
        this.inventoryService = inventoryService;
        this.inventoryItemRepository = inventoryItemRepository;
        this.cacheService = cacheService;
    }

    @GetMapping
    public ApiResponse<PageResult<Inventory>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.ok(inventoryService.listInventories(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Inventory> getById(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.getInventoryDetail(id));
    }

    @GetMapping("/{id}/items")
    public ApiResponse<List<InventoryItem>> getItems(@PathVariable Long id) {
        return ApiResponse.ok(inventoryItemRepository.findByInventoryId(id));
    }

    @GetMapping("/{id}/progress")
    public ApiResponse<InventoryProgressDTO> getProgress(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.getInventoryProgress(id));
    }

    @PostMapping
    public ApiResponse<Inventory> create(@RequestBody InventoryDTO dto) {
        if (dto.getInventoryDate() == null) {
            dto.setInventoryDate(LocalDate.now());
        }
        Inventory result = inventoryService.createInventory(dto);
        cacheService.evictStatsCache();
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<Inventory> update(@PathVariable Long id, @RequestBody InventoryDTO dto) {
        Inventory result = inventoryService.updateInventory(id, dto);
        cacheService.evictStatsCache();
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/item")
    public ApiResponse<InventoryItem> updateItem(@PathVariable Long id, @RequestBody InventoryItemDTO dto) {
        InventoryItem result = inventoryService.updateItem(id, dto);
        cacheService.evictStatsCache();
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<InventoryCompletionResultDTO> complete(@PathVariable Long id) {
        InventoryCompletionResultDTO result = inventoryService.completeInventory(id);
        cacheService.evictStatsCache();
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}/differences")
    public ApiResponse<List<DifferenceGroupDTO>> getDifferences(
            @PathVariable Long id,
            @RequestParam(defaultValue = "category") String groupBy) {
        return ApiResponse.ok(inventoryService.getDifferenceGroups(id, groupBy));
    }
}
