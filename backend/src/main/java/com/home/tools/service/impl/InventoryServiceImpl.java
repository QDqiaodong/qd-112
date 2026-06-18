package com.home.tools.service.impl;

import com.home.tools.dto.DifferenceGroupDTO;
import com.home.tools.dto.DifferenceItemDTO;
import com.home.tools.dto.InventoryCompletionResultDTO;
import com.home.tools.dto.InventoryDTO;
import com.home.tools.dto.InventoryItemDTO;
import com.home.tools.dto.InventoryProgressDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    @Transactional
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
        inventory.setMismatchedTools(0);
        inventory.setUncheckedTools(allTools.size());
        inventory.setCompleted(false);

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
    @Transactional
    public InventoryItem updateItem(Long inventoryId, InventoryItemDTO dto) {
        List<InventoryItem> items = inventoryItemRepository.findByInventoryId(inventoryId);
        InventoryItem item = items.stream()
                .filter(i -> i.getToolId().equals(dto.getToolId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("InventoryItem not found"));
        if (dto.getActualStatus() != null) {
            item.setActualStatus(dto.getActualStatus());
        }
        if (dto.getChecked() != null) {
            item.setChecked(dto.getChecked());
        }
        if (dto.getRemarks() != null) {
            item.setRemarks(dto.getRemarks());
        }

        InventoryItem saved = inventoryItemRepository.save(item);

        updateInventoryStats(inventoryId);

        return saved;
    }

    private void updateInventoryStats(Long inventoryId) {
        List<InventoryItem> allItems = inventoryItemRepository.findByInventoryId(inventoryId);
        long checkedCount = allItems.stream().filter(InventoryItem::getChecked).count();
        long uncheckedCount = allItems.stream().filter(i -> !i.getChecked()).count();
        long mismatchedCount = allItems.stream()
                .filter(i -> !i.getExpectedStatus().equals(i.getActualStatus())).count();
        long lostCount = allItems.stream()
                .filter(i -> "LOST".equals(i.getActualStatus())).count();

        Inventory inventory = inventoryRepository.findById(inventoryId).orElse(null);
        if (inventory != null) {
            inventory.setCheckedTools((int) checkedCount);
            inventory.setUncheckedTools((int) uncheckedCount);
            inventory.setMismatchedTools((int) mismatchedCount);
            inventory.setLostTools((int) lostCount);
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public InventoryProgressDTO getInventoryProgress(Long inventoryId) {
        List<InventoryItem> allItems = inventoryItemRepository.findByInventoryId(inventoryId);
        int total = allItems.size();

        int checkedCount = 0;
        int uncheckedCount = 0;
        int mismatchedCount = 0;
        int lostCount = 0;

        for (InventoryItem item : allItems) {
            boolean isMismatched = !item.getExpectedStatus().equals(item.getActualStatus());
            boolean isLost = "LOST".equals(item.getActualStatus());

            if (item.getChecked()) {
                checkedCount++;
            } else {
                uncheckedCount++;
            }
            if (isMismatched) {
                mismatchedCount++;
            }
            if (isLost) {
                lostCount++;
            }
        }

        InventoryProgressDTO progress = new InventoryProgressDTO();
        progress.setTotalTools(total);
        progress.setCheckedCount(checkedCount);
        progress.setUncheckedCount(uncheckedCount);
        progress.setMismatchedCount(mismatchedCount);
        progress.setLostCount(lostCount);

        if (total > 0) {
            progress.setCheckedPercent(Math.round(checkedCount * 10000.0 / total) / 100.0);
            progress.setUncheckedPercent(Math.round(uncheckedCount * 10000.0 / total) / 100.0);
            progress.setMismatchedPercent(Math.round(mismatchedCount * 10000.0 / total) / 100.0);
            progress.setLostPercent(Math.round(lostCount * 10000.0 / total) / 100.0);
        } else {
            progress.setCheckedPercent(0.0);
            progress.setUncheckedPercent(0.0);
            progress.setMismatchedPercent(0.0);
            progress.setLostPercent(0.0);
        }

        return progress;
    }

    @Override
    @Transactional
    public InventoryCompletionResultDTO completeInventory(Long inventoryId) {
        InventoryCompletionResultDTO result = new InventoryCompletionResultDTO();
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (Boolean.TRUE.equals(inventory.getCompleted())) {
            result.setSuccess(false);
            result.setMessage("该盘点已完成，无需重复操作");
            return result;
        }

        List<InventoryItem> allItems = inventoryItemRepository.findByInventoryId(inventoryId);

        int checkedCount = 0;
        int uncheckedCount = 0;
        int mismatchedCount = 0;
        int lostCount = 0;
        int updatedCount = 0;

        Map<Long, InventoryItem> itemByToolId = new HashMap<>();
        for (InventoryItem item : allItems) {
            itemByToolId.put(item.getToolId(), item);
            boolean isMismatched = !item.getExpectedStatus().equals(item.getActualStatus());
            boolean isLost = "LOST".equals(item.getActualStatus());

            if (item.getChecked()) {
                checkedCount++;
            } else {
                uncheckedCount++;
            }
            if (isMismatched) {
                mismatchedCount++;
            }
            if (isLost) {
                lostCount++;
            }
        }

        List<Long> toolIds = new ArrayList<>(itemByToolId.keySet());
        List<Tool> tools = toolRepository.findAllById(toolIds);

        for (Tool tool : tools) {
            InventoryItem item = itemByToolId.get(tool.getId());
            if (item == null) continue;

            String actualStatusStr = item.getActualStatus();
            try {
                ToolStatus actualStatus = ToolStatus.valueOf(actualStatusStr);
                if (!tool.getStatus().equals(actualStatus)) {
                    tool.setStatus(actualStatus);
                    toolRepository.save(tool);
                    updatedCount++;
                }
            } catch (IllegalArgumentException e) {
                // 无效状态，跳过
            }
        }

        inventory.setCheckedTools(checkedCount);
        inventory.setUncheckedTools(uncheckedCount);
        inventory.setMismatchedTools(mismatchedCount);
        inventory.setLostTools(lostCount);
        inventory.setCompleted(true);
        inventory.setCompleteTime(LocalDateTime.now());
        inventoryRepository.save(inventory);

        result.setSuccess(true);
        result.setMessage("盘点完成，已根据盘点结果更新工具状态");
        result.setTotalUpdated(updatedCount);
        result.setStatusMismatchCount(mismatchedCount);
        result.setLostCount(lostCount);
        result.setCheckedCount(checkedCount);
        result.setUncheckedCount(uncheckedCount);

        return result;
    }

    @Override
    public List<DifferenceGroupDTO> getDifferenceGroups(Long inventoryId, String groupBy) {
        List<InventoryItem> allItems = inventoryItemRepository.findByInventoryId(inventoryId);

        Map<String, List<InventoryItem>> allGrouped;
        if ("location".equals(groupBy)) {
            allGrouped = allItems.stream()
                    .collect(Collectors.groupingBy(
                            item -> LocationUtils.normalizeLocationForDisplay(item.getLocation()),
                            LinkedHashMap::new, Collectors.toList()));
        } else {
            allGrouped = allItems.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getCategoryName() != null ? item.getCategoryName() : "未分类",
                            LinkedHashMap::new, Collectors.toList()));
        }

        List<DifferenceGroupDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<InventoryItem>> entry : allGrouped.entrySet()) {
            List<InventoryItem> groupItems = entry.getValue();

            int groupTotal = groupItems.size();
            int groupChecked = 0;
            int groupUnchecked = 0;
            int groupMismatched = 0;
            int groupLost = 0;

            List<InventoryItem> differenceItems = new ArrayList<>();
            for (InventoryItem item : groupItems) {
                boolean isMismatched = !item.getExpectedStatus().equals(item.getActualStatus());
                boolean isUnchecked = !item.getChecked();
                boolean isLost = "LOST".equals(item.getActualStatus());
                boolean isMaintenance = "MAINTENANCE".equals(item.getActualStatus());

                if (item.getChecked()) {
                    groupChecked++;
                } else {
                    groupUnchecked++;
                }
                if (isMismatched) {
                    groupMismatched++;
                }
                if (isLost) {
                    groupLost++;
                }

                if (isMismatched || isUnchecked || isLost || isMaintenance) {
                    differenceItems.add(item);
                }
            }

            DifferenceGroupDTO group = new DifferenceGroupDTO();
            group.setGroupKey(entry.getKey());
            group.setGroupType(groupBy);
            group.setTotalCount(groupTotal);
            group.setCheckedCount(groupChecked);
            group.setUncheckedCount(groupUnchecked);
            group.setMismatchedCount(groupMismatched);
            group.setLostCount(groupLost);
            group.setCompletionPercent(groupTotal > 0 ? Math.round(groupChecked * 10000.0 / groupTotal) / 100.0 : 0.0);

            List<DifferenceItemDTO> diffItems = differenceItems.stream().map(item -> {
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
