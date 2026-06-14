package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.InventoryDTO;
import com.home.tools.dto.InventoryItemDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.Inventory;
import com.home.tools.entity.InventoryItem;
import com.home.tools.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
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

    @PostMapping
    public ApiResponse<Inventory> create(@RequestBody InventoryDTO dto) {
        if (dto.getInventoryDate() == null) {
            dto.setInventoryDate(LocalDate.now());
        }
        return ApiResponse.ok(inventoryService.createInventory(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<Inventory> update(@PathVariable Long id, @RequestBody InventoryDTO dto) {
        return ApiResponse.ok(inventoryService.updateInventory(id, dto));
    }

    @PostMapping("/{id}/item")
    public ApiResponse<InventoryItem> updateItem(@PathVariable Long id, @RequestBody InventoryItemDTO dto) {
        return ApiResponse.ok(inventoryService.updateItem(id, dto));
    }
}
