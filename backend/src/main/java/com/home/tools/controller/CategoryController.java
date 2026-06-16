package com.home.tools.controller;

import com.home.tools.dto.ApiResponse;
import com.home.tools.dto.CategoryDeletionCheck;
import com.home.tools.entity.*;
import com.home.tools.repository.CategoryRepository;
import com.home.tools.repository.MaintenanceRecordRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.repository.ToolStatusHistoryRepository;
import com.home.tools.repository.UsageRecordRepository;
import com.home.tools.service.CategoryService;
import com.home.tools.service.CacheService;
import com.home.tools.service.ToolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;
    private final CacheService cacheService;
    private final ToolService toolService;
    private final ToolRepository toolRepository;
    private final CategoryRepository categoryRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final ToolStatusHistoryRepository toolStatusHistoryRepository;
    private final ObjectMapper objectMapper;

    public CategoryController(CategoryService categoryService,
                              CacheService cacheService,
                              ToolService toolService,
                              ToolRepository toolRepository,
                              CategoryRepository categoryRepository,
                              MaintenanceRecordRepository maintenanceRecordRepository,
                              UsageRecordRepository usageRecordRepository,
                              ToolStatusHistoryRepository toolStatusHistoryRepository,
                              ObjectMapper objectMapper) {
        this.categoryService = categoryService;
        this.cacheService = cacheService;
        this.toolService = toolService;
        this.toolRepository = toolRepository;
        this.categoryRepository = categoryRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.usageRecordRepository = usageRecordRepository;
        this.toolStatusHistoryRepository = toolStatusHistoryRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/categories/tree")
    public ApiResponse<List<Map<String, Object>>> getCategoryTree() {
        return ApiResponse.ok(categoryService.getCategoryTree());
    }

    @GetMapping("/maintenance-items")
    public ApiResponse<List<MaintenanceItem>> listMaintenanceItems() {
        return ApiResponse.ok(categoryService.listMaintenanceItems());
    }

    @GetMapping("/stats/overview")
    public ApiResponse<Map<String, Object>> getStatsOverview() {
        String cached = cacheService.getStatsOverviewFromCache();
        if (cached != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = objectMapper.readValue(cached, Map.class);
                return ApiResponse.ok(data);
            } catch (Exception ignored) {
            }
        }

        Map<String, Object> stats = new HashMap<>();
        Map<String, Long> statusCounts = toolService.countByStatus();
        stats.put("statusCounts", statusCounts);

        List<Tool> dueTools = toolService.findDueMaintenance();
        stats.put("dueMaintenanceCount", (long) dueTools.size());

        List<Map<String, Object>> categoryStats = buildCategoryStats();
        stats.put("categoryStats", categoryStats);

        List<Map<String, Object>> recentActivities = buildRecentActivities();
        stats.put("recentActivities", recentActivities);

        List<Map<String, Object>> dueMaintenanceTools = buildDueMaintenanceTools(dueTools);
        stats.put("dueMaintenanceTools", dueMaintenanceTools);

        try {
            cacheService.cacheStatsOverview(objectMapper.writeValueAsString(stats));
        } catch (Exception ignored) {
        }

        return ApiResponse.ok(stats);
    }

    private List<Map<String, Object>> buildCategoryStats() {
        List<Tool> allTools = toolRepository.findAll();
        Map<Long, String> categoryNames = new HashMap<>();
        for (Category cat : categoryRepository.findAll()) {
            categoryNames.put(cat.getId(), cat.getName());
        }
        Map<String, Long> counts = allTools.stream()
                .filter(t -> t.getCategoryId() != null)
                .collect(Collectors.groupingBy(
                        t -> categoryNames.getOrDefault(t.getCategoryId(), "未分类"),
                        LinkedHashMap::new,
                        Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<ToolStatusHistory> histories = toolStatusHistoryRepository
                .findAll().stream()
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .limit(5)
                .collect(Collectors.toList());
        for (ToolStatusHistory h : histories) {
            Map<String, Object> a = new HashMap<>();
            a.put("id", h.getId());
            a.put("type", "STATUS_CHANGE");
            a.put("content", "工具状态变更：" + getStatusName(h.getOldStatus()) + " → " + getStatusName(h.getNewStatus()));
            a.put("time", h.getCreateTime().format(fmt));
            activities.add(a);
        }

        List<MaintenanceRecord> records = maintenanceRecordRepository
                .findAll().stream()
                .sorted((a, b) -> {
                    if (b.getCreateTime() == null && a.getCreateTime() == null) return 0;
                    if (b.getCreateTime() == null) return -1;
                    if (a.getCreateTime() == null) return 1;
                    return b.getCreateTime().compareTo(a.getCreateTime());
                })
                .limit(5)
                .collect(Collectors.toList());
        for (MaintenanceRecord r : records) {
            Map<String, Object> a = new HashMap<>();
            a.put("id", r.getId());
            a.put("type", "MAINTENANCE");
            a.put("content", "保养记录：" + r.getDescription());
            a.put("time", r.getCreateTime() != null ? r.getCreateTime().format(fmt) : "");
            activities.add(a);
        }

        List<UsageRecord> usages = usageRecordRepository
                .findAll().stream()
                .sorted((a, b) -> {
                    if (b.getCreateTime() == null && a.getCreateTime() == null) return 0;
                    if (b.getCreateTime() == null) return -1;
                    if (a.getCreateTime() == null) return 1;
                    return b.getCreateTime().compareTo(a.getCreateTime());
                })
                .limit(5)
                .collect(Collectors.toList());
        for (UsageRecord u : usages) {
            Map<String, Object> a = new HashMap<>();
            a.put("id", u.getId());
            a.put("type", "USAGE");
            a.put("content", "使用记录：" + (u.getScenario() != null ? u.getScenario() : ""));
            a.put("time", u.getCreateTime() != null ? u.getCreateTime().format(fmt) : "");
            activities.add(a);
        }

        activities.sort((a, b) -> {
            String tA = (String) a.get("time");
            String tB = (String) b.get("time");
            if (tA == null && tB == null) return 0;
            if (tA == null) return 1;
            if (tB == null) return -1;
            return tB.compareTo(tA);
        });

        return activities.stream().limit(10).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildDueMaintenanceTools(List<Tool> dueTools) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Tool t : dueTools) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("nextMaintenanceDate", t.getNextMaintenanceDate() != null ? t.getNextMaintenanceDate().toString() : "");
            list.add(m);
        }
        return list;
    }

    private String getStatusName(ToolStatus status) {
        if (status == null) return "未知";
        return switch (status) {
            case AVAILABLE -> "可用";
            case IN_USE -> "使用中";
            case MAINTENANCE -> "保养中";
            case LOANED -> "借出";
            case LOST -> "丢失";
        };
    }

    @GetMapping("/categories/{id}/deletion-check")
    public ApiResponse<CategoryDeletionCheck> checkDeletion(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.checkDeletion(id));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.delete(id);
            return ApiResponse.ok(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
