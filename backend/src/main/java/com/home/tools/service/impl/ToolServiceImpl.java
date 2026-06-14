package com.home.tools.service.impl;

import com.home.tools.dto.MaintenanceTrackDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.dto.ToolDTO;
import com.home.tools.entity.*;
import com.home.tools.repository.*;
import com.home.tools.service.ToolService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ToolServiceImpl implements ToolService {

    private final ToolRepository toolRepository;
    private final ToolStatusHistoryRepository statusHistoryRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryRepository inventoryRepository;

    public ToolServiceImpl(ToolRepository toolRepository,
                           ToolStatusHistoryRepository statusHistoryRepository,
                           UsageRecordRepository usageRecordRepository,
                           MaintenanceRecordRepository maintenanceRecordRepository,
                           InventoryItemRepository inventoryItemRepository,
                           InventoryRepository inventoryRepository) {
        this.toolRepository = toolRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.usageRecordRepository = usageRecordRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public PageResult<Tool> list(Integer page, Integer size, String keyword, Long categoryId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Tool> result;
        if (categoryId != null) {
            result = toolRepository.findByCategoryId(categoryId, pageable);
        } else if (StringUtils.hasText(keyword)) {
            result = toolRepository.findByNameContainingOrModelContainingOrBrandContaining(keyword, keyword, keyword, pageable);
        } else {
            result = toolRepository.findAll(pageable);
        }
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    @Override
    public Tool getById(Long id) {
        return toolRepository.findById(id).orElseThrow(() -> new RuntimeException("Tool not found"));
    }

    @Override
    public Tool create(ToolDTO dto) {
        Tool tool = new Tool();
        copyDtoToEntity(dto, tool);
        if (dto.getStatus() != null) {
            tool.setStatus(ToolStatus.valueOf(dto.getStatus()));
        } else {
            tool.setStatus(ToolStatus.AVAILABLE);
        }
        if (tool.getNextMaintenanceDate() == null && tool.getPurchaseDate() != null && tool.getMaintenanceCycleDays() != null) {
            tool.setNextMaintenanceDate(tool.getPurchaseDate().plusDays(tool.getMaintenanceCycleDays()));
        }
        return toolRepository.save(tool);
    }

    @Override
    public Tool update(Long id, ToolDTO dto) {
        Tool tool = getById(id);
        ToolStatus oldStatus = tool.getStatus();
        copyDtoToEntity(dto, tool);
        ToolStatus newStatus = oldStatus;
        if (dto.getStatus() != null) {
            newStatus = ToolStatus.valueOf(dto.getStatus());
            tool.setStatus(newStatus);
        }
        Tool saved = toolRepository.save(tool);
        if (oldStatus != newStatus) {
            ToolStatusHistory history = new ToolStatusHistory();
            history.setToolId(id);
            history.setOldStatus(oldStatus);
            history.setNewStatus(newStatus);
            history.setOperator(dto.getOperator());
            history.setReason(dto.getStatusReason());
            statusHistoryRepository.save(history);
        }
        return saved;
    }

    @Override
    public void delete(Long id) {
        toolRepository.deleteById(id);
    }

    @Override
    public List<Tool> findByCategory(Long categoryId) {
        return toolRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Tool> findDueMaintenance() {
        return toolRepository.findByNextMaintenanceDateBefore(LocalDate.now());
    }

    @Override
    public Map<String, Long> countByStatus() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ToolStatus status : ToolStatus.values()) {
            counts.put(status.name(), (long) toolRepository.findByStatus(status).size());
        }
        return counts;
    }

    private void copyDtoToEntity(ToolDTO dto, Tool tool) {
        tool.setName(dto.getName());
        tool.setModel(dto.getModel());
        tool.setBrand(dto.getBrand());
        tool.setCategoryId(dto.getCategoryId());
        tool.setSubCategoryId(dto.getSubCategoryId());
        tool.setPurpose(dto.getPurpose());
        tool.setSpecification(dto.getSpecification());
        tool.setLocation(dto.getLocation());
        tool.setPurchaseDate(dto.getPurchaseDate());
        tool.setPrice(dto.getPrice());
        tool.setLastMaintenanceDate(dto.getLastMaintenanceDate());
        tool.setNextMaintenanceDate(dto.getNextMaintenanceDate());
        tool.setMaintenanceCycleDays(dto.getMaintenanceCycleDays());
    }

    @Override
    public List<MaintenanceTrackDTO> getMaintenanceTrack(Long toolId) {
        List<MaintenanceTrackDTO> tracks = new ArrayList<>();

        List<UsageRecord> usageRecords = usageRecordRepository.findByToolIdOrderByUseDateDesc(toolId);
        for (UsageRecord record : usageRecords) {
            MaintenanceTrackDTO dto = new MaintenanceTrackDTO();
            dto.setType(MaintenanceTrackDTO.TrackType.USAGE);
            dto.setRecordId(record.getId());
            dto.setActionDate(record.getUseDate());
            dto.setActionTime(record.getCreateTime());
            dto.setActionName("使用");
            dto.setDescription(record.getScenario());
            dto.setDurationMinutes(record.getDurationMinutes());
            dto.setCost(BigDecimal.ZERO);
            dto.setOperator(record.getOperator());
            tracks.add(dto);
        }

        List<MaintenanceRecord> maintenanceRecords = maintenanceRecordRepository.findByToolIdOrderByMaintenanceDateDesc(toolId);
        for (MaintenanceRecord record : maintenanceRecords) {
            MaintenanceTrackDTO dto = new MaintenanceTrackDTO();
            dto.setType(MaintenanceTrackDTO.TrackType.MAINTENANCE);
            dto.setRecordId(record.getId());
            dto.setActionDate(record.getMaintenanceDate());
            dto.setActionTime(record.getCreateTime());
            dto.setMaintenanceType(record.getMaintenanceType());
            dto.setActionName(getMaintenanceTypeName(record.getMaintenanceType()));
            dto.setDescription(record.getDescription());
            dto.setCost(record.getCost() != null ? record.getCost() : BigDecimal.ZERO);
            dto.setOperator(record.getOperator());
            tracks.add(dto);
        }

        List<InventoryItem> inventoryItems = inventoryItemRepository.findByToolIdWithInventory(toolId);
        Map<Long, Inventory> inventoryMap = inventoryRepository.findAll().stream()
                .collect(Collectors.toMap(Inventory::getId, i -> i));
        for (InventoryItem item : inventoryItems) {
            Inventory inventory = inventoryMap.get(item.getInventoryId());
            if (inventory != null) {
                MaintenanceTrackDTO dto = new MaintenanceTrackDTO();
                dto.setType(MaintenanceTrackDTO.TrackType.INVENTORY);
                dto.setRecordId(item.getId());
                dto.setActionDate(inventory.getInventoryDate());
                dto.setActionTime(inventory.getCreateTime());
                dto.setActionName("盘点");
                dto.setDescription(item.getRemarks());
                dto.setCost(BigDecimal.ZERO);
                dto.setOperator(inventory.getOperator());
                dto.setInventoryChecked(item.getChecked());
                dto.setInventoryActualStatus(item.getActualStatus());
                tracks.add(dto);
            }
        }

        List<ToolStatusHistory> statusHistories = statusHistoryRepository.findByToolIdOrderByCreateTimeDesc(toolId);
        for (ToolStatusHistory history : statusHistories) {
            MaintenanceTrackDTO dto = new MaintenanceTrackDTO();
            dto.setType(MaintenanceTrackDTO.TrackType.STATUS_CHANGE);
            dto.setRecordId(history.getId());
            dto.setActionDate(history.getCreateTime().toLocalDate());
            dto.setActionTime(history.getCreateTime());
            dto.setActionName("状态变更");
            dto.setDescription(history.getReason());
            dto.setCost(BigDecimal.ZERO);
            dto.setOperator(history.getOperator());
            dto.setOldStatus(history.getOldStatus());
            dto.setNewStatus(history.getNewStatus());
            tracks.add(dto);
        }

        tracks.sort((a, b) -> {
            LocalDateTime timeA = a.getActionTime() != null ? a.getActionTime() : a.getActionDate().atStartOfDay();
            LocalDateTime timeB = b.getActionTime() != null ? b.getActionTime() : b.getActionDate().atStartOfDay();
            return timeB.compareTo(timeA);
        });

        return tracks;
    }

    private String getMaintenanceTypeName(MaintenanceType type) {
        return switch (type) {
            case CLEAN -> "清洁";
            case OIL -> "润滑";
            case TIGHTEN -> "紧固";
            case INSPECT -> "检查";
            case REPAIR -> "修理";
            case OTHER -> "其他";
        };
    }
}
