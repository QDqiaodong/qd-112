package com.home.tools.service.impl;

import com.home.tools.dto.CostSummaryDTO;
import com.home.tools.entity.Category;
import com.home.tools.entity.MaintenanceRecord;
import com.home.tools.entity.PartReplacement;
import com.home.tools.entity.Tool;
import com.home.tools.repository.CategoryRepository;
import com.home.tools.repository.MaintenanceRecordRepository;
import com.home.tools.repository.PartReplacementRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.CostSummaryService;
import com.home.tools.util.LocationUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CostSummaryServiceImpl implements CostSummaryService {

    private final ToolRepository toolRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final PartReplacementRepository partReplacementRepository;
    private final CategoryRepository categoryRepository;

    public CostSummaryServiceImpl(ToolRepository toolRepository,
                                  MaintenanceRecordRepository maintenanceRecordRepository,
                                  PartReplacementRepository partReplacementRepository,
                                  CategoryRepository categoryRepository) {
        this.toolRepository = toolRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.partReplacementRepository = partReplacementRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CostSummaryDTO> getSummaryByCategory() {
        List<Tool> allTools = toolRepository.findAll();
        List<MaintenanceRecord> allMaintenance = maintenanceRecordRepository.findAll();
        List<PartReplacement> allParts = partReplacementRepository.findAll();

        Map<Long, String> categoryNames = new HashMap<>();
        for (Category cat : categoryRepository.findAll()) {
            categoryNames.put(cat.getId(), cat.getName());
        }

        Map<Long, Tool> toolMap = allTools.stream()
                .collect(Collectors.toMap(Tool::getId, t -> t));

        Map<String, List<Tool>> toolsByCategory = allTools.stream()
                .collect(Collectors.groupingBy(
                        t -> categoryNames.getOrDefault(t.getCategoryId(), "未分类")));

        Map<String, List<MaintenanceRecord>> maintenanceByCategory = allMaintenance.stream()
                .collect(Collectors.groupingBy(
                        m -> {
                            Tool t = toolMap.get(m.getToolId());
                            return t != null ? categoryNames.getOrDefault(t.getCategoryId(), "未分类") : "未分类";
                        }));

        Map<String, List<PartReplacement>> partsByCategory = allParts.stream()
                .collect(Collectors.groupingBy(
                        p -> {
                            Tool t = toolMap.get(p.getToolId());
                            return t != null ? categoryNames.getOrDefault(t.getCategoryId(), "未分类") : "未分类";
                        }));

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(toolsByCategory.keySet());
        allKeys.addAll(maintenanceByCategory.keySet());
        allKeys.addAll(partsByCategory.keySet());

        List<CostSummaryDTO> result = new ArrayList<>();
        for (String key : allKeys) {
            CostSummaryDTO dto = new CostSummaryDTO();
            dto.setGroupKey(key);
            dto.setGroupType("CATEGORY");

            BigDecimal purchaseCost = toolsByCategory.getOrDefault(key, Collections.emptyList()).stream()
                    .map(t -> t.getPrice() != null ? t.getPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setPurchaseCost(purchaseCost);

            BigDecimal maintenanceCost = maintenanceByCategory.getOrDefault(key, Collections.emptyList()).stream()
                    .map(m -> m.getCost() != null ? m.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setMaintenanceCost(maintenanceCost);

            BigDecimal partCost = partsByCategory.getOrDefault(key, Collections.emptyList()).stream()
                    .map(p -> p.getCost() != null ? p.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setPartReplacementCost(partCost);

            dto.setTotalCost(purchaseCost.add(maintenanceCost).add(partCost));
            dto.setToolCount((long) toolsByCategory.getOrDefault(key, Collections.emptyList()).size());

            result.add(dto);
        }

        result.sort((a, b) -> b.getTotalCost().compareTo(a.getTotalCost()));
        return result;
    }

    @Override
    public List<CostSummaryDTO> getSummaryByLocation() {
        List<Tool> allTools = toolRepository.findAll();
        List<MaintenanceRecord> allMaintenance = maintenanceRecordRepository.findAll();
        List<PartReplacement> allParts = partReplacementRepository.findAll();

        Map<Long, Tool> toolMap = allTools.stream()
                .collect(Collectors.toMap(Tool::getId, t -> t));

        Map<String, List<Tool>> toolsByLocation = allTools.stream()
                .collect(Collectors.groupingBy(
                        t -> LocationUtils.normalizeLocationForDisplay(t.getLocation())));

        Map<String, List<MaintenanceRecord>> maintenanceByLocation = allMaintenance.stream()
                .collect(Collectors.groupingBy(
                        m -> {
                            Tool t = toolMap.get(m.getToolId());
                            return t != null ? LocationUtils.normalizeLocationForDisplay(t.getLocation()) : "未知";
                        }));

        Map<String, List<PartReplacement>> partsByLocation = allParts.stream()
                .collect(Collectors.groupingBy(
                        p -> {
                            Tool t = toolMap.get(p.getToolId());
                            return t != null ? LocationUtils.normalizeLocationForDisplay(t.getLocation()) : "未知";
                        }));

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(toolsByLocation.keySet());
        allKeys.addAll(maintenanceByLocation.keySet());
        allKeys.addAll(partsByLocation.keySet());

        List<CostSummaryDTO> result = new ArrayList<>();
        for (String key : allKeys) {
            CostSummaryDTO dto = new CostSummaryDTO();
            dto.setGroupKey(key);
            dto.setGroupType("LOCATION");

            BigDecimal purchaseCost = toolsByLocation.getOrDefault(key, Collections.emptyList()).stream()
                    .map(t -> t.getPrice() != null ? t.getPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setPurchaseCost(purchaseCost);

            BigDecimal maintenanceCost = maintenanceByLocation.getOrDefault(key, Collections.emptyList()).stream()
                    .map(m -> m.getCost() != null ? m.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setMaintenanceCost(maintenanceCost);

            BigDecimal partCost = partsByLocation.getOrDefault(key, Collections.emptyList()).stream()
                    .map(p -> p.getCost() != null ? p.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setPartReplacementCost(partCost);

            dto.setTotalCost(purchaseCost.add(maintenanceCost).add(partCost));
            dto.setToolCount((long) toolsByLocation.getOrDefault(key, Collections.emptyList()).size());

            result.add(dto);
        }

        result.sort((a, b) -> b.getTotalCost().compareTo(a.getTotalCost()));
        return result;
    }

    @Override
    public List<CostSummaryDTO> getSummaryByMonth() {
        List<Tool> allTools = toolRepository.findAll();
        List<MaintenanceRecord> allMaintenance = maintenanceRecordRepository.findAll();
        List<PartReplacement> allParts = partReplacementRepository.findAll();

        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, List<Tool>> toolsByMonth = allTools.stream()
                .filter(t -> t.getPurchaseDate() != null)
                .collect(Collectors.groupingBy(t -> t.getPurchaseDate().format(monthFmt)));

        Map<String, List<MaintenanceRecord>> maintenanceByMonth = allMaintenance.stream()
                .filter(m -> m.getMaintenanceDate() != null)
                .collect(Collectors.groupingBy(m -> m.getMaintenanceDate().format(monthFmt)));

        Map<String, List<PartReplacement>> partsByMonth = allParts.stream()
                .filter(p -> p.getReplacementDate() != null)
                .collect(Collectors.groupingBy(p -> p.getReplacementDate().format(monthFmt)));

        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(toolsByMonth.keySet());
        allKeys.addAll(maintenanceByMonth.keySet());
        allKeys.addAll(partsByMonth.keySet());

        List<CostSummaryDTO> result = new ArrayList<>();
        for (String key : allKeys) {
            CostSummaryDTO dto = new CostSummaryDTO();
            dto.setGroupKey(key);
            dto.setGroupType("MONTH");

            BigDecimal purchaseCost = toolsByMonth.getOrDefault(key, Collections.emptyList()).stream()
                    .map(t -> t.getPrice() != null ? t.getPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setPurchaseCost(purchaseCost);

            BigDecimal maintenanceCost = maintenanceByMonth.getOrDefault(key, Collections.emptyList()).stream()
                    .map(m -> m.getCost() != null ? m.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setMaintenanceCost(maintenanceCost);

            BigDecimal partCost = partsByMonth.getOrDefault(key, Collections.emptyList()).stream()
                    .map(p -> p.getCost() != null ? p.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setPartReplacementCost(partCost);

            dto.setTotalCost(purchaseCost.add(maintenanceCost).add(partCost));
            dto.setToolCount((long) toolsByMonth.getOrDefault(key, Collections.emptyList()).size());

            result.add(dto);
        }

        return result;
    }
}
