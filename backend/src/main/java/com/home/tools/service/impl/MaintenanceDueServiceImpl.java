package com.home.tools.service.impl;

import com.home.tools.dto.MaintenanceDashboardStats;
import com.home.tools.dto.MaintenanceDueResult;
import com.home.tools.entity.*;
import com.home.tools.repository.CategoryMaintenanceItemRepository;
import com.home.tools.repository.CategoryRepository;
import com.home.tools.repository.MaintenanceItemRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.CacheService;
import com.home.tools.service.MaintenanceDueService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaintenanceDueServiceImpl implements MaintenanceDueService {

    private static final int APPROACHING_THRESHOLD_DAYS = 7;
    private static final int SEVERELY_OVERDUE_THRESHOLD_DAYS = 30;
    private static final int LONG_TERM_OVERDUE_THRESHOLD_DAYS = 90;

    private final ToolRepository toolRepository;
    private final CategoryRepository categoryRepository;
    private final MaintenanceItemRepository maintenanceItemRepository;
    private final CategoryMaintenanceItemRepository categoryMaintenanceItemRepository;
    private final CacheService cacheService;

    public MaintenanceDueServiceImpl(ToolRepository toolRepository,
                                     CategoryRepository categoryRepository,
                                     MaintenanceItemRepository maintenanceItemRepository,
                                     CategoryMaintenanceItemRepository categoryMaintenanceItemRepository,
                                     CacheService cacheService) {
        this.toolRepository = toolRepository;
        this.categoryRepository = categoryRepository;
        this.maintenanceItemRepository = maintenanceItemRepository;
        this.categoryMaintenanceItemRepository = categoryMaintenanceItemRepository;
        this.cacheService = cacheService;
    }

    @Override
    public MaintenanceDueResult calculateForTool(Long toolId) {
        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> new RuntimeException("Tool not found: " + toolId));
        return calculateForTool(tool);
    }

    @Override
    public MaintenanceDueResult calculateForTool(Tool tool) {
        MaintenanceDueResult result = new MaintenanceDueResult();
        result.setToolId(tool.getId());
        result.setToolName(tool.getName());
        result.setCycleDays(tool.getMaintenanceCycleDays());
        result.setLastMaintenanceDate(tool.getLastMaintenanceDate());
        result.setStoredDueDate(tool.getNextMaintenanceDate());

        Integer effectiveCycle = resolveEffectiveCycle(tool, result);
        result.setEffectiveCycleDays(effectiveCycle);

        LocalDate calculatedDueDate = calculateDueDate(
                tool.getLastMaintenanceDate(),
                tool.getPurchaseDate(),
                effectiveCycle
        );
        result.setCalculatedDueDate(calculatedDueDate);

        MaintenanceDueStatus status = determineStatus(
                effectiveCycle,
                tool.getLastMaintenanceDate(),
                tool.getPurchaseDate(),
                tool.getNextMaintenanceDate()
        );
        result.setStatus(status);
        result.setStatusDescription(buildStatusDescription(status, calculatedDueDate, effectiveCycle));

        LocalDate today = LocalDate.now();
        if (calculatedDueDate != null && status != MaintenanceDueStatus.NO_CYCLE) {
            if (calculatedDueDate.isAfter(today)) {
                int daysRemaining = (int) ChronoUnit.DAYS.between(today, calculatedDueDate);
                result.setDaysRemaining(daysRemaining);
                result.setDaysOverdue(0);
            } else if (calculatedDueDate.isEqual(today)) {
                result.setDaysRemaining(0);
                result.setDaysOverdue(0);
            } else {
                int daysOverdue = (int) ChronoUnit.DAYS.between(calculatedDueDate, today);
                result.setDaysRemaining(0);
                result.setDaysOverdue(daysOverdue);
            }
        } else {
            result.setDaysRemaining(null);
            result.setDaysOverdue(null);
        }

        return result;
    }

    @Override
    public List<MaintenanceDueResult> calculateForAllTools() {
        List<Tool> tools = toolRepository.findAll();
        return tools.stream()
                .map(this::calculateForTool)
                .sorted(Comparator.comparingInt(r -> -r.getStatus().getSeverity()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceDueResult> calculateForTools(List<Long> toolIds) {
        List<Tool> tools = toolRepository.findAllById(toolIds);
        return tools.stream()
                .map(this::calculateForTool)
                .sorted(Comparator.comparingInt(r -> -r.getStatus().getSeverity()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceDueResult> calculateByCategory(Long categoryId) {
        List<Tool> direct = toolRepository.findByCategoryId(categoryId);
        Set<Long> childIds = findChildCategoryIds(categoryId);
        List<Tool> subTools = new ArrayList<>(direct);
        for (Long childId : childIds) {
            subTools.addAll(toolRepository.findByCategoryId(childId));
        }
        return subTools.stream()
                .map(this::calculateForTool)
                .sorted(Comparator.comparingInt(r -> -r.getStatus().getSeverity()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceDueResult> findByStatus(MaintenanceDueStatus status) {
        return calculateForAllTools().stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceDueResult> findDueOrOverdue() {
        return calculateForAllTools().stream()
                .filter(r -> r.getStatus().isDueOrOverdue())
                .sorted(Comparator.comparingInt(r -> -r.getStatus().getSeverity()))
                .collect(Collectors.toList());
    }

    @Override
    public MaintenanceDashboardStats getDashboardStats() {
        List<MaintenanceDueResult> all = calculateForAllTools();

        MaintenanceDashboardStats stats = new MaintenanceDashboardStats();
        stats.setTotalTools(all.size());

        Map<MaintenanceDueStatus, Long> counts = all.stream()
                .collect(Collectors.groupingBy(MaintenanceDueResult::getStatus, Collectors.counting()));

        stats.setNormalCount(counts.getOrDefault(MaintenanceDueStatus.NORMAL, 0L));
        stats.setApproachingCount(counts.getOrDefault(MaintenanceDueStatus.APPROACHING, 0L));
        stats.setDueTodayCount(counts.getOrDefault(MaintenanceDueStatus.DUE_TODAY, 0L));
        stats.setOverdueCount(counts.getOrDefault(MaintenanceDueStatus.OVERDUE, 0L));
        stats.setSeverelyOverdueCount(counts.getOrDefault(MaintenanceDueStatus.SEVERELY_OVERDUE, 0L));
        stats.setLongTermOverdueCount(counts.getOrDefault(MaintenanceDueStatus.LONG_TERM_OVERDUE, 0L));
        stats.setNoCycleCount(counts.getOrDefault(MaintenanceDueStatus.NO_CYCLE, 0L));

        long dueOrOverdue = stats.getDueTodayCount() + stats.getOverdueCount()
                + stats.getSeverelyOverdueCount() + stats.getLongTermOverdueCount();
        stats.setDueOrOverdueTotal(dueOrOverdue);

        Map<Long, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        Map<String, Long> byCategory = new LinkedHashMap<>();
        Map<Long, Long> byCategoryId = all.stream()
                .filter(r -> r.getToolId() != null)
                .filter(r -> r.getStatus().isDueOrOverdue() || r.getStatus() == MaintenanceDueStatus.APPROACHING)
                .collect(Collectors.groupingBy(r -> {
                    Tool tool = toolRepository.findById(r.getToolId()).orElse(null);
                    return tool != null ? tool.getCategoryId() : null;
                }, Collectors.counting()));

        for (Map.Entry<Long, Long> entry : byCategoryId.entrySet()) {
            if (entry.getKey() != null) {
                String name = categoryNames.getOrDefault(entry.getKey(), "未知分类");
                byCategory.put(name, entry.getValue());
            }
        }
        stats.setByCategory(byCategory);

        long effectiveTotal = stats.getTotalTools() - stats.getNoCycleCount();
        if (effectiveTotal > 0) {
            double rate = (double) dueOrOverdue / effectiveTotal * 100;
            stats.setOverdueRate(Math.round(rate * 100.0) / 100.0);
        } else {
            stats.setOverdueRate(0.0);
        }

        return stats;
    }

    @Override
    public MaintenanceDueStatus determineStatus(Integer effectiveCycleDays,
                                                LocalDate lastMaintenanceDate,
                                                LocalDate purchaseDate,
                                                LocalDate storedDueDate) {
        if (effectiveCycleDays == null || effectiveCycleDays <= 0) {
            return MaintenanceDueStatus.NO_CYCLE;
        }

        LocalDate dueDate = storedDueDate;
        if (dueDate == null) {
            dueDate = calculateDueDate(lastMaintenanceDate, purchaseDate, effectiveCycleDays);
        }
        if (dueDate == null) {
            return MaintenanceDueStatus.NO_CYCLE;
        }

        LocalDate today = LocalDate.now();
        if (dueDate.isAfter(today)) {
            long daysUntil = ChronoUnit.DAYS.between(today, dueDate);
            if (daysUntil <= APPROACHING_THRESHOLD_DAYS) {
                return MaintenanceDueStatus.APPROACHING;
            }
            return MaintenanceDueStatus.NORMAL;
        } else if (dueDate.isEqual(today)) {
            return MaintenanceDueStatus.DUE_TODAY;
        } else {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);
            if (daysOverdue > LONG_TERM_OVERDUE_THRESHOLD_DAYS) {
                return MaintenanceDueStatus.LONG_TERM_OVERDUE;
            } else if (daysOverdue > SEVERELY_OVERDUE_THRESHOLD_DAYS) {
                return MaintenanceDueStatus.SEVERELY_OVERDUE;
            }
            return MaintenanceDueStatus.OVERDUE;
        }
    }

    private Integer resolveEffectiveCycle(Tool tool, MaintenanceDueResult result) {
        if (tool.getMaintenanceCycleDays() != null && tool.getMaintenanceCycleDays() > 0) {
            result.setCycleSource("工具自定义");
            return tool.getMaintenanceCycleDays();
        }

        if (tool.getSubCategoryId() != null) {
            Integer subCycle = getCategoryDefaultCycle(tool.getSubCategoryId());
            if (subCycle != null) {
                Category cat = categoryRepository.findById(tool.getSubCategoryId()).orElse(null);
                String catName = cat != null ? cat.getName() : "子分类";
                result.setCycleSource(catName + "默认周期");
                return subCycle;
            }
        }

        if (tool.getCategoryId() != null) {
            Integer parentCycle = getCategoryDefaultCycle(tool.getCategoryId());
            if (parentCycle != null) {
                Category cat = categoryRepository.findById(tool.getCategoryId()).orElse(null);
                String catName = cat != null ? cat.getName() : "分类";
                result.setCycleSource(catName + "默认周期");
                return parentCycle;
            }
        }

        Map<String, String> cachedCycles = cacheService.getMaintenanceCyclesFromCache();
        if (cachedCycles != null && !cachedCycles.isEmpty()) {
            Collection<String> values = cachedCycles.values();
            OptionalDouble avg = values.stream()
                    .filter(v -> v != null && !v.isEmpty())
                    .mapToInt(v -> {
                        try { return Integer.parseInt(v); }
                        catch (Exception e) { return 0; }
                    })
                    .filter(i -> i > 0)
                    .average();
            if (avg.isPresent()) {
                result.setCycleSource("系统平均周期");
                return (int) Math.round(avg.getAsDouble());
            }
        }

        List<MaintenanceItem> items = maintenanceItemRepository.findAll();
        if (!items.isEmpty()) {
            OptionalDouble avg = items.stream()
                    .filter(i -> i.getDefaultCycleDays() != null && i.getDefaultCycleDays() > 0)
                    .mapToInt(MaintenanceItem::getDefaultCycleDays)
                    .average();
            if (avg.isPresent()) {
                result.setCycleSource("保养项平均周期");
                return (int) Math.round(avg.getAsDouble());
            }
        }

        result.setCycleSource("无");
        return null;
    }

    private Integer getCategoryDefaultCycle(Long categoryId) {
        List<CategoryMaintenanceItem> items = categoryMaintenanceItemRepository.findByCategoryId(categoryId);
        if (items == null || items.isEmpty()) {
            return null;
        }
        OptionalDouble avg = items.stream()
                .filter(CategoryMaintenanceItem::getEnabled)
                .map(item -> item.getCustomCycleDays() != null
                        ? item.getCustomCycleDays()
                        : resolveMaintenanceItemDefaultCycle(item.getMaintenanceItemCode()))
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .filter(i -> i > 0)
                .average();
        return avg.isPresent() ? (int) Math.round(avg.getAsDouble()) : null;
    }

    private Integer resolveMaintenanceItemDefaultCycle(String code) {
        if (code == null) return null;
        Map<String, String> cached = cacheService.getMaintenanceCyclesFromCache();
        if (cached != null && cached.containsKey(code)) {
            try {
                return Integer.parseInt(cached.get(code));
            } catch (Exception ignored) {}
        }
        return maintenanceItemRepository.findByCode(code)
                .map(MaintenanceItem::getDefaultCycleDays)
                .orElse(null);
    }

    private LocalDate calculateDueDate(LocalDate lastMaintenanceDate,
                                       LocalDate purchaseDate,
                                       Integer cycleDays) {
        if (cycleDays == null || cycleDays <= 0) {
            return null;
        }
        LocalDate baseDate = lastMaintenanceDate != null ? lastMaintenanceDate : purchaseDate;
        if (baseDate == null) {
            return null;
        }
        return baseDate.plusDays(cycleDays);
    }

    private String buildStatusDescription(MaintenanceDueStatus status,
                                          LocalDate dueDate,
                                          Integer cycleDays) {
        LocalDate today = LocalDate.now();
        if (status == MaintenanceDueStatus.NO_CYCLE) {
            return "未设置保养周期";
        }
        if (dueDate == null) {
            return "无法计算到期日期（缺少基准日期）";
        }
        if (status == MaintenanceDueStatus.NORMAL) {
            long days = ChronoUnit.DAYS.between(today, dueDate);
            return String.format("保养正常，距离下次保养还有 %d 天（到期日：%s）", days, dueDate);
        }
        if (status == MaintenanceDueStatus.APPROACHING) {
            long days = ChronoUnit.DAYS.between(today, dueDate);
            return String.format("保养即将到期，还剩 %d 天（到期日：%s）", days, dueDate);
        }
        if (status == MaintenanceDueStatus.DUE_TODAY) {
            return String.format("今日到期，建议尽快安排保养（到期日：%s）", dueDate);
        }
        long overdue = ChronoUnit.DAYS.between(dueDate, today);
        if (status == MaintenanceDueStatus.OVERDUE) {
            return String.format("已逾期 %d 天，请尽快保养（到期日：%s）", overdue, dueDate);
        }
        if (status == MaintenanceDueStatus.SEVERELY_OVERDUE) {
            return String.format("严重逾期 %d 天，需立即保养（到期日：%s）", overdue, dueDate);
        }
        return String.format("长期逾期 %d 天，建议停用检修（到期日：%s）", overdue, dueDate);
    }

    private Set<Long> findChildCategoryIds(Long parentId) {
        Set<Long> ids = new HashSet<>();
        List<Category> all = categoryRepository.findAll();
        for (Category cat : all) {
            if (parentId.equals(cat.getParentId())) {
                ids.add(cat.getId());
            }
        }
        return ids;
    }
}
