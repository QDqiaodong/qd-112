package com.home.tools.service.impl;

import com.home.tools.dto.InventoryDTO;
import com.home.tools.dto.InventoryItemDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.Inventory;
import com.home.tools.entity.InventoryItem;
import com.home.tools.entity.Tool;
import com.home.tools.entity.ToolStatus;
import com.home.tools.repository.InventoryItemRepository;
import com.home.tools.repository.InventoryRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ToolRepository toolRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                InventoryItemRepository inventoryItemRepository,
                                ToolRepository toolRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.toolRepository = toolRepository;
    }

    @Override
    public PageResult<Inventory> listInventories(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Inventory> result = inventoryRepository.findAll(pageable);
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    @Override
    public Inventory getInventoryDetail(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
    }

    @Override
    public Inventory createInventory(InventoryDTO dto) {
        Inventory inventory = new Inventory();
        inventory.setInventoryDate(dto.getInventoryDate());
        inventory.setOperator(dto.getOperator());
        inventory.setRemarks(dto.getRemarks());

        List<Tool> allTools = toolRepository.findAll();
        inventory.setTotalTools(allTools.size());

        Map<String, Integer> statusCounts = new HashMap<>();
        for (ToolStatus status : ToolStatus.values()) {
            statusCounts.put(status.name(), 0);
        }
        for (Tool tool : allTools) {
            statusCounts.merge(tool.getStatus().name(), 1, Integer::sum);
        }
        inventory.setAvailableTools(statusCounts.get(ToolStatus.AVAILABLE.name()));
        inventory.setLoanedTools(statusCounts.get(ToolStatus.LOANED.name()));
        inventory.setMaintenanceTools(statusCounts.get(ToolStatus.MAINTENANCE.name()));
        inventory.setLostTools(statusCounts.get(ToolStatus.LOST.name()));
        inventory.setCheckedTools(0);

        Inventory saved = inventoryRepository.save(inventory);

        for (Tool tool : allTools) {
            InventoryItem item = new InventoryItem();
            item.setInventoryId(saved.getId());
            item.setToolId(tool.getId());
            item.setExpectedStatus(tool.getStatus().name());
            item.setActualStatus(tool.getStatus().name());
            item.setChecked(false);
            item.setRemarks("");
            inventoryItemRepository.save(item);
        }

        return saved;
    }

    @Override
    public Inventory updateInventory(Long id, InventoryDTO dto) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        inventory.setOperator(dto.getOperator());
        inventory.setRemarks(dto.getRemarks());
        return inventoryRepository.save(inventory);
    }

    @Override
    public InventoryItem updateItem(Long inventoryId, InventoryItemDTO dto) {
        List<InventoryItem> items = inventoryItemRepository.findByInventoryId(inventoryId);
        InventoryItem item = items.stream()
                .filter(i -> i.getToolId().equals(dto.getToolId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("InventoryItem not found"));
        item.setActualStatus(dto.getActualStatus());
        item.setChecked(dto.getChecked());
        item.setRemarks(dto.getRemarks());

        InventoryItem saved = inventoryItemRepository.save(item);

        List<InventoryItem> allItems = inventoryItemRepository.findByInventoryId(inventoryId);
        long checkedCount = allItems.stream().filter(InventoryItem::getChecked).count();
        Inventory inventory = inventoryRepository.findById(inventoryId).orElse(null);
        if (inventory != null) {
            inventory.setCheckedTools((int) checkedCount);
            inventoryRepository.save(inventory);
        }

        return saved;
    }
}
