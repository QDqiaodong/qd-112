package com.home.tools.service.impl;

import com.home.tools.dto.MaintenanceTrackDTO;
import com.home.tools.dto.PageResult;
import com.home.tools.dto.StatusTransitionResult;
import com.home.tools.dto.ToolAvailabilityScore;
import com.home.tools.dto.ToolDTO;
import com.home.tools.dto.ToolWithScore;
import com.home.tools.entity.*;
import com.home.tools.repository.*;
import com.home.tools.service.ToolService;
import com.home.tools.util.LocationUtils;
import com.home.tools.util.StatusTransitionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    public PageResult<Tool> list(Integer page, Integer size, String keyword, Long categoryId, Long subCategoryId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Tool> result;
        if (subCategoryId != null) {
            result = toolRepository.findBySubCategoryId(subCategoryId, pageable);
        } else if (categoryId != null) {
            result = toolRepository.findByCategoryId(categoryId, pageable);
        } else if (StringUtils.hasText(keyword)) {
            result = toolRepository.findByNameContainingOrModelContainingOrBrandContaining(keyword, keyword, keyword, pageable);
        } else {
            result = toolRepository.findAll(pageable);
        }
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, size);
    }

    @Override
    public PageResult<ToolWithScore> listWithScore(Integer page, Integer size, String keyword, Long categoryId, Long subCategoryId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Tool> result;
        if (subCategoryId != null) {
            result = toolRepository.findBySubCategoryId(subCategoryId, pageable);
        } else if (categoryId != null) {
            result = toolRepository.findByCategoryId(categoryId, pageable);
        } else if (StringUtils.hasText(keyword)) {
            result = toolRepository.findByNameContainingOrModelContainingOrBrandContaining(keyword, keyword, keyword, pageable);
        } else {
            result = toolRepository.findAll(pageable);
        }
        List<ToolWithScore> listWithScores = result.getContent().stream()
                .map(tool -> new ToolWithScore(tool, calculateAvailabilityScore(tool)))
                .collect(Collectors.toList());
        return new PageResult<>(listWithScores, result.getTotalElements(), page, size);
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
    @Transactional
    public Tool update(Long id, ToolDTO dto) {
        Tool tool = getById(id);
        ToolStatus oldStatus = tool.getStatus();
        LocalDate oldPurchaseDate = tool.getPurchaseDate();
        Integer oldMaintenanceCycleDays = tool.getMaintenanceCycleDays();
        LocalDate oldLastMaintenanceDate = tool.getLastMaintenanceDate();
        
        copyDtoToEntity(dto, tool);
        
        boolean purchaseDateChanged = !Objects.equals(oldPurchaseDate, tool.getPurchaseDate());
        boolean cycleChanged = !Objects.equals(oldMaintenanceCycleDays, tool.getMaintenanceCycleDays());
        boolean lastMaintenanceChanged = !Objects.equals(oldLastMaintenanceDate, tool.getLastMaintenanceDate());
        boolean nextMaintenanceExplicitlySet = dto.getNextMaintenanceDate() != null;
        
        if ((purchaseDateChanged || cycleChanged || lastMaintenanceChanged) && !nextMaintenanceExplicitlySet) {
            LocalDate baseDate = tool.getLastMaintenanceDate() != null ? tool.getLastMaintenanceDate() : tool.getPurchaseDate();
            if (baseDate != null && tool.getMaintenanceCycleDays() != null && tool.getMaintenanceCycleDays() > 0) {
                tool.setNextMaintenanceDate(baseDate.plusDays(tool.getMaintenanceCycleDays()));
            }
        }
        
        ToolStatus newStatus = oldStatus;
        if (dto.getStatus() != null) {
            newStatus = ToolStatus.valueOf(dto.getStatus());
            StatusTransitionResult validation = StatusTransitionUtil.validate(oldStatus, newStatus);
            if (!validation.isValid()) {
                throw new RuntimeException(validation.getMessage() + "：" + validation.getReason());
            }
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
    @Transactional
    public void delete(Long id) {
        inventoryItemRepository.deleteByToolId(id);
        usageRecordRepository.deleteByToolId(id);
        maintenanceRecordRepository.deleteByToolId(id);
        statusHistoryRepository.deleteByToolId(id);
        toolRepository.deleteById(id);
    }

    @Override
    public List<Tool> findByCategory(Long categoryId) {
        return toolRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Tool> findDueMaintenance() {
        return toolRepository.findByNextMaintenanceDateLessThanEqual(LocalDate.now());
    }

    @Override
    public List<Tool> findMaintenanceByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return toolRepository.findByNextMaintenanceDateBetween(startDate, endDate);
    }

    @Override
    public Map<String, Long> countByStatus() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ToolStatus status : ToolStatus.values()) {
            counts.put(status.name(), (long) toolRepository.findByStatus(status).size());
        }
        return counts;
    }

    @Override
    public Map<String, Long> countByLocation() {
        List<Tool> allTools = toolRepository.findAll();
        Map<String, Long> counts = allTools.stream()
                .collect(Collectors.groupingBy(
                        tool -> LocationUtils.normalizeLocationForDisplay(tool.getLocation()),
                        LinkedHashMap::new,
                        Collectors.counting()));
        return counts;
    }

    @Override
    public StatusTransitionResult validateStatusTransition(Long toolId, ToolStatus newStatus) {
        Tool tool = getById(toolId);
        return StatusTransitionUtil.validate(tool.getStatus(), newStatus);
    }

    @Override
    public List<ToolStatus> getAllowedStatusTransitions(ToolStatus currentStatus) {
        return StatusTransitionUtil.getAllowedTargets(currentStatus);
    }

    private void copyDtoToEntity(ToolDTO dto, Tool tool) {
        tool.setName(dto.getName());
        tool.setModel(dto.getModel());
        tool.setBrand(dto.getBrand());
        tool.setCategoryId(dto.getCategoryId());
        tool.setSubCategoryId(dto.getSubCategoryId());
        tool.setPurpose(dto.getPurpose());
        tool.setSpecification(dto.getSpecification());
        tool.setLocation(LocationUtils.normalizeLocation(dto.getLocation()));
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

    @Override
    public ToolAvailabilityScore calculateAvailabilityScore(Long toolId) {
        Tool tool = getById(toolId);
        return calculateAvailabilityScore(tool);
    }

    @Override
    public ToolAvailabilityScore calculateAvailabilityScore(Tool tool) {
        ToolAvailabilityScore score = new ToolAvailabilityScore();
        score.setToolId(tool.getId());

        int statusScore = calculateStatusScore(tool, score);
        int maintenanceScore = calculateMaintenanceScore(tool, score);
        int usageScore = calculateUsageScore(tool, score);
        int inventoryScore = calculateInventoryScore(tool, score);

        int totalScore = statusScore + maintenanceScore + usageScore + inventoryScore;
        score.setTotalScore(totalScore);

        return score;
    }

    private int calculateStatusScore(Tool tool, ToolAvailabilityScore score) {
        ToolStatus status = tool.getStatus();
        int statusScore;
        String detail;

        switch (status) {
            case AVAILABLE:
                statusScore = 30;
                detail = "状态：可用 - 工具随时可使用";
                break;
            case IN_USE:
                statusScore = 25;
                detail = "状态：使用中 - 工具正在被使用";
                break;
            case LOANED:
                statusScore = 20;
                detail = "状态：借出 - 工具已借出，暂不可用";
                break;
            case MAINTENANCE:
                statusScore = 15;
                detail = "状态：保养中 - 工具正在维护保养";
                break;
            case LOST:
                statusScore = 0;
                detail = "状态：丢失 - 工具已丢失";
                break;
            default:
                statusScore = 0;
                detail = "状态：未知";
        }

        score.setStatusScore(statusScore);
        score.setStatusDetail(detail);
        return statusScore;
    }

    private int calculateMaintenanceScore(Tool tool, ToolAvailabilityScore score) {
        int maintenanceScore;
        String detail;
        int overdueDays = 0;

        LocalDate nextMaintenanceDate = tool.getNextMaintenanceDate();
        score.setNextMaintenanceDate(nextMaintenanceDate);

        if (tool.getMaintenanceCycleDays() == null || nextMaintenanceDate == null) {
            maintenanceScore = 25;
            detail = "保养：无保养周期要求";
        } else {
            LocalDate today = LocalDate.now();
            if (nextMaintenanceDate.isAfter(today)) {
                long daysUntil = ChronoUnit.DAYS.between(today, nextMaintenanceDate);
                if (daysUntil <= 7) {
                    maintenanceScore = 22;
                    detail = String.format("保养：即将到期，还剩 %d 天", daysUntil);
                } else if (daysUntil <= 30) {
                    maintenanceScore = 24;
                    detail = String.format("保养：状态良好，距离下次保养还有 %d 天", daysUntil);
                } else {
                    maintenanceScore = 25;
                    detail = String.format("保养：状态良好，距离下次保养还有 %d 天", daysUntil);
                }
            } else if (nextMaintenanceDate.isEqual(today)) {
                maintenanceScore = 20;
                detail = "保养：今日到期，建议尽快安排保养";
            } else {
                overdueDays = (int) ChronoUnit.DAYS.between(nextMaintenanceDate, today);
                if (overdueDays <= 7) {
                    maintenanceScore = 18;
                    detail = String.format("保养：逾期 %d 天，请尽快保养", overdueDays);
                } else if (overdueDays <= 30) {
                    maintenanceScore = 12;
                    detail = String.format("保养：逾期 %d 天，需立即保养", overdueDays);
                } else if (overdueDays <= 90) {
                    maintenanceScore = 5;
                    detail = String.format("保养：严重逾期 %d 天，存在安全隐患", overdueDays);
                } else {
                    maintenanceScore = 0;
                    detail = String.format("保养：长期逾期 %d 天，建议停用检修", overdueDays);
                }
            }
        }

        score.setMaintenanceScore(maintenanceScore);
        score.setMaintenanceDetail(detail);
        score.setOverdueDays(overdueDays);
        return maintenanceScore;
    }

    private int calculateUsageScore(Tool tool, ToolAvailabilityScore score) {
        int usageScore;
        String detail;

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        LocalDate ninetyDaysAgo = today.minusDays(90);

        Long count30 = usageRecordRepository.countByToolIdAndUseDateBetween(tool.getId(), thirtyDaysAgo, today);
        Long count90 = usageRecordRepository.countByToolIdAndUseDateBetween(tool.getId(), ninetyDaysAgo, today);
        LocalDate lastUseDate = usageRecordRepository.findLastUseDateByToolId(tool.getId());

        score.setUsageCount30Days(count30 != null ? count30.intValue() : 0);
        score.setUsageCount90Days(count90 != null ? count90.intValue() : 0);
        score.setLastUseDate(lastUseDate);

        int count30Int = count30 != null ? count30.intValue() : 0;
        int count90Int = count90 != null ? count90.intValue() : 0;

        if (count30Int >= 5) {
            usageScore = 25;
            detail = String.format("使用：近30天使用 %d 次，使用频率高，工具状态活跃", count30Int);
        } else if (count30Int >= 3) {
            usageScore = 22;
            detail = String.format("使用：近30天使用 %d 次，使用频率较高", count30Int);
        } else if (count30Int >= 1) {
            usageScore = 20;
            detail = String.format("使用：近30天使用 %d 次，使用频率正常", count30Int);
        } else if (count90Int >= 1) {
            usageScore = 15;
            detail = String.format("使用：近90天使用 %d 次，使用频率较低", count90Int);
        } else if (lastUseDate != null) {
            long daysSinceLastUse = ChronoUnit.DAYS.between(lastUseDate, today);
            if (daysSinceLastUse <= 180) {
                usageScore = 10;
                detail = String.format("使用：距上次使用 %d 天，长期未使用", daysSinceLastUse);
            } else {
                usageScore = 5;
                detail = String.format("使用：距上次使用 %d 天，超长期闲置", daysSinceLastUse);
            }
        } else {
            usageScore = 8;
            detail = "使用：暂无使用记录";
        }

        score.setUsageScore(usageScore);
        score.setUsageDetail(detail);
        return usageScore;
    }

    private int calculateInventoryScore(Tool tool, ToolAvailabilityScore score) {
        int inventoryScore;
        String detail;

        List<InventoryItem> inventoryItems = inventoryItemRepository.findByToolIdWithInventory(tool.getId());

        if (inventoryItems == null || inventoryItems.isEmpty()) {
            inventoryScore = 12;
            detail = "盘点：暂无盘点记录，建议纳入盘点计划";
            score.setInventoryScore(inventoryScore);
            score.setInventoryDetail(detail);
            return inventoryScore;
        }

        Map<Long, Inventory> inventoryMap = inventoryRepository.findAll().stream()
                .collect(Collectors.toMap(Inventory::getId, i -> i));

        InventoryItem latestItem = inventoryItems.get(0);
        Inventory latestInventory = inventoryMap.get(latestItem.getInventoryId());

        if (latestInventory != null) {
            score.setLastInventoryDate(latestInventory.getInventoryDate());
        }
        score.setLastInventoryChecked(latestItem.getChecked());
        score.setLastInventoryActualStatus(latestItem.getActualStatus());
        score.setLastInventoryExpectedStatus(latestItem.getExpectedStatus());

        Boolean checked = latestItem.getChecked();
        String expectedStatus = latestItem.getExpectedStatus();
        String actualStatus = latestItem.getActualStatus();

        if (checked == null || !checked) {
            inventoryScore = 8;
            detail = "盘点：最近盘点未核对，需确认工具状态";
        } else if (actualStatus == null) {
            inventoryScore = 10;
            detail = "盘点：已核对但未记录实际状态";
        } else if (expectedStatus != null && expectedStatus.equals(actualStatus)) {
            inventoryScore = 20;
            detail = String.format("盘点：最近盘点状态一致（%s）", getStatusName(actualStatus));
        } else if ("LOST".equals(actualStatus)) {
            inventoryScore = 0;
            detail = "盘点：最近盘点发现工具丢失";
        } else if ("MAINTENANCE".equals(actualStatus) && !"MAINTENANCE".equals(expectedStatus)) {
            inventoryScore = 12;
            detail = String.format("盘点：状态有差异，预期：%s，实际：%s（保养中）",
                    getStatusName(expectedStatus), getStatusName(actualStatus));
        } else {
            inventoryScore = 14;
            detail = String.format("盘点：状态有差异，预期：%s，实际：%s",
                    getStatusName(expectedStatus), getStatusName(actualStatus));
        }

        score.setInventoryScore(inventoryScore);
        score.setInventoryDetail(detail);
        return inventoryScore;
    }

    private String getStatusName(String status) {
        if (status == null) return "未知";
        return switch (status) {
            case "AVAILABLE" -> "可用";
            case "IN_USE" -> "使用中";
            case "MAINTENANCE" -> "保养中";
            case "LOANED" -> "借出";
            case "LOST" -> "丢失";
            default -> status;
        };
    }
}
