package com.home.tools.service.impl;

import com.home.tools.dto.DifferenceGroupDTO;
import com.home.tools.dto.DifferenceItemDTO;
import com.home.tools.dto.InventoryDTO;
import com.home.tools.dto.InventoryItemDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.entity.Category;
import com.home.tools.entity.Inventory;
import com.home.tools.entity.InventoryItem;
import com.home.tools.entity.Tool;
import com.home.tools.entity.ToolStatus;
import com.home.tools.repository.CategoryRepository;
import com.home.tools.repository.InventoryItemRepository;
import com.home.tools.repository.InventoryRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.InventoryService;
import com.home.tools.util.LocationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ToolRepository toolRepository;
    private final CategoryRepository categoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                InventoryItemRepository inventoryItemRepository,
                                ToolRepository toolRepository,
                                CategoryRepository categoryRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.toolRepository = toolRepository;
        this.categoryRepository = categoryRepository;
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

        Map<Long, String> categoryNames = new HashMap<>();
        for (Category cat : categoryRepository.findAll()) {
            categoryNames.put(cat.getId(), cat.getName());
        }

        for (Tool tool : allTools) {
            InventoryItem item = new InventoryItem();
            item.setInventoryId(saved.getId());
            item.setToolId(tool.getId());
            item.setExpectedStatus(tool.getStatus().name());
            item.setActualStatus(tool.getStatus().name());
            item.setChecked(false);
            item.setRemarks("");
            item.setToolName(tool.getName());
            item.setToolModel(tool.getModel());
            item.setToolBrand(tool.getBrand());
            item.setCategoryId(tool.getCategoryId());
            item.setCategoryName(categoryNames.getOrDefault(tool.getCategoryId(), "未分类"));
            item.setLocation(LocationUtils.normalizeLocation(tool.getLocation()));
            item.setSnapshotStatus(tool.getStatus().name());
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

    @Override
    public List<DifferenceGroupDTO> getDifferenceGroups(Long inventoryId, String groupBy) {
        List<InventoryItem> allItems = inventoryItemRepository.findByInventoryId(inventoryId);

        List<InventoryItem> differenceItems = allItems.stream()
                .filter(item -> {
                    boolean statusMismatch = !item.getExpectedStatus().equals(item.getActualStatus());
                    boolean unchecked = !item.getChecked();
                    boolean lost = "LOST".equals(item.getActualStatus());
                    boolean maintenance = "MAINTENANCE".equals(item.getActualStatus());
                    return statusMismatch || unchecked || lost || maintenance;
                })
                .collect(Collectors.toList());

        Map<String, List<InventoryItem>> grouped;
        if ("location".equals(groupBy)) {
            grouped = differenceItems.stream()
                    .collect(Collectors.groupingBy(
                            item -> LocationUtils.normalizeLocationForDisplay(item.getLocation()),
                            LinkedHashMap::new, Collectors.toList()));
        } else {
            grouped = differenceItems.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getCategoryName() != null ? item.getCategoryName() : "未分类",
                            LinkedHashMap::new, Collectors.toList()));
        }

        List<DifferenceGroupDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<InventoryItem>> entry : grouped.entrySet()) {
            DifferenceGroupDTO group = new DifferenceGroupDTO();
            group.setGroupKey(entry.getKey());
            group.setGroupType(groupBy);
            List<DifferenceItemDTO> diffItems = entry.getValue().stream().map(item -> {
                DifferenceItemDTO diff = new DifferenceItemDTO();
                diff.setToolId(item.getToolId());
                diff.setToolName(item.getToolName());
                diff.setToolModel(item.getToolModel());
                diff.setToolBrand(item.getToolBrand());
                diff.setExpectedStatus(item.getExpectedStatus());
                diff.setActualStatus(item.getActualStatus());
                diff.setChecked(item.getChecked());
                diff.setCategoryName(item.getCategoryName());
                diff.setLocation(LocationUtils.normalizeLocationForDisplay(item.getLocation()));
                diff.setRemarks(item.getRemarks());

                List<String> types = new ArrayList<>();
                if (!item.getExpectedStatus().equals(item.getActualStatus())) {
                    types.add("STATUS_MISMATCH");
                }
                if (!item.getChecked()) {
                    types.add("UNCHECKED");
                }
                if ("LOST".equals(item.getActualStatus())) {
                    types.add("LOST");
                }
                if ("MAINTENANCE".equals(item.getActualStatus())) {
                    types.add("MAINTENANCE");
                }
                diff.setDifferenceType(String.join(",", types));
                return diff;
            }).collect(Collectors.toList());
            group.setItems(diffItems);
            result.add(group);
        }

        return result;
    }
}
