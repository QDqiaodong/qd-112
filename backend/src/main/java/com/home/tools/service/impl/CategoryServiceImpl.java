package com.home.tools.service.impl;

import com.home.tools.dto.CategoryDeletionCheck;
import com.home.tools.dto.CategoryTreeNode;
import com.home.tools.entity.Category;
import com.home.tools.entity.CategoryMaintenanceItem;
import com.home.tools.entity.MaintenanceItem;
import com.home.tools.entity.Tool;
import com.home.tools.repository.CategoryMaintenanceItemRepository;
import com.home.tools.repository.CategoryRepository;
import com.home.tools.repository.MaintenanceItemRepository;
import com.home.tools.repository.MaintenanceRecordRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.CacheService;
import com.home.tools.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final MaintenanceItemRepository maintenanceItemRepository;
    private final ToolRepository toolRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final CategoryMaintenanceItemRepository categoryMaintenanceItemRepository;
    private final CacheService cacheService;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               MaintenanceItemRepository maintenanceItemRepository,
                               ToolRepository toolRepository,
                               MaintenanceRecordRepository maintenanceRecordRepository,
                               CategoryMaintenanceItemRepository categoryMaintenanceItemRepository,
                               CacheService cacheService) {
        this.categoryRepository = categoryRepository;
        this.maintenanceItemRepository = maintenanceItemRepository;
        this.toolRepository = toolRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.categoryMaintenanceItemRepository = categoryMaintenanceItemRepository;
        this.cacheService = cacheService;
    }

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        List<Map<String, Object>> cached = cacheService.getCategoryTreeFromCache();
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        List<Category> all = categoryRepository.findAll();
        List<Map<String, Object>> tree = buildTree(all, null);
        cacheService.cacheCategoryTree(all);
        return tree;
    }

    @Override
    public List<CategoryTreeNode> getCategoryTreeWithStats() {
        List<Category> allCategories = categoryRepository.findAll();
        List<Tool> allTools = toolRepository.findAll();
        List<MaintenanceItem> allMaintenanceItems = maintenanceItemRepository.findAll();
        List<CategoryMaintenanceItem> allCategoryItems = categoryMaintenanceItemRepository.findAll();

        Map<Long, Integer> toolCountMap = new HashMap<>();
        Map<Long, Integer> maintenanceCountMap = new HashMap<>();
        Map<Long, Integer> defaultCycleDaysMap = new HashMap<>();

        for (Category cat : allCategories) {
            toolCountMap.put(cat.getId(), 0);
            maintenanceCountMap.put(cat.getId(), 0);
            defaultCycleDaysMap.put(cat.getId(), null);
        }

        Map<String, Integer> itemCycleMap = new HashMap<>();
        for (MaintenanceItem mi : allMaintenanceItems) {
            itemCycleMap.put(mi.getCode(), mi.getDefaultCycleDays() != null ? mi.getDefaultCycleDays() : 0);
        }

        Map<Long, Integer> categoryMinCycleMap = new HashMap<>();
        for (CategoryMaintenanceItem cmi : allCategoryItems) {
            if (cmi.getEnabled() != null && cmi.getEnabled()) {
                int cycle;
                if (cmi.getCustomCycleDays() != null) {
                    cycle = cmi.getCustomCycleDays();
                } else {
                    cycle = itemCycleMap.getOrDefault(cmi.getMaintenanceItemCode(), 0);
                }
                if (cycle > 0) {
                    categoryMinCycleMap.merge(cmi.getCategoryId(), cycle, Math::min);
                }
            }
        }
        for (Map.Entry<Long, Integer> entry : categoryMinCycleMap.entrySet()) {
            defaultCycleDaysMap.put(entry.getKey(), entry.getValue());
        }

        for (Tool tool : allTools) {
            if (tool.getCategoryId() != null) {
                toolCountMap.merge(tool.getCategoryId(), 1, Integer::sum);
            }
            if (tool.getSubCategoryId() != null) {
                toolCountMap.merge(tool.getSubCategoryId(), 1, Integer::sum);
            }
        }

        for (CategoryMaintenanceItem item : allCategoryItems) {
            if (item.getEnabled() != null && item.getEnabled()) {
                maintenanceCountMap.merge(item.getCategoryId(), 1, Integer::sum);
            }
        }

        int totalMaintenanceItems = allMaintenanceItems.size();

        List<CategoryTreeNode> tree = buildTreeWithStats(
            allCategories, null, toolCountMap, maintenanceCountMap, totalMaintenanceItems, defaultCycleDaysMap
        );

        tree.sort(Comparator.comparing(n -> n.getSortOrder() != null ? n.getSortOrder() : 0));

        return tree;
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    @Transactional
    public Category create(Category category) {
        if (category.getParentId() != null) {
            Category parent = getById(category.getParentId());
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setLevel(1);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        Category saved = categoryRepository.save(category);
        cacheService.evictCategoryCache();
        cacheService.cacheCategoryTree(categoryRepository.findAll());
        return saved;
    }

    @Override
    @Transactional
    public Category update(Long id, Category category) {
        Category existing = getById(id);
        existing.setName(category.getName());
        existing.setCode(category.getCode());
        existing.setDescription(category.getDescription());
        if (category.getSortOrder() != null) {
            existing.setSortOrder(category.getSortOrder());
        }
        Category saved = categoryRepository.save(existing);
        cacheService.evictCategoryCache();
        cacheService.cacheCategoryTree(categoryRepository.findAll());
        return saved;
    }

    @Override
    @Transactional
    public void updateSortOrder(Long id, Integer sortOrder) {
        Category category = getById(id);
        category.setSortOrder(sortOrder);
        categoryRepository.save(category);
        cacheService.evictCategoryCache();
        cacheService.cacheCategoryTree(categoryRepository.findAll());
    }

    @Override
    public List<MaintenanceItem> listMaintenanceItems() {
        return maintenanceItemRepository.findAll();
    }

    private List<Map<String, Object>> buildTree(List<Category> all, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Category cat : all) {
            boolean isChild = (parentId == null && cat.getParentId() == null) ||
                    (parentId != null && parentId.equals(cat.getParentId()));
            if (isChild) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", cat.getId());
                node.put("name", cat.getName());
                node.put("code", cat.getCode());
                node.put("parentId", cat.getParentId());
                node.put("level", cat.getLevel());
                node.put("sortOrder", cat.getSortOrder());
                node.put("description", cat.getDescription());
                node.put("children", buildTree(all, cat.getId()));
                tree.add(node);
            }
        }
        return tree;
    }

    private List<CategoryTreeNode> buildTreeWithStats(List<Category> all, Long parentId,
                                                       Map<Long, Integer> toolCountMap,
                                                       Map<Long, Integer> maintenanceCountMap,
                                                       int totalMaintenanceItems,
                                                       Map<Long, Integer> defaultCycleDaysMap) {
        List<CategoryTreeNode> tree = new ArrayList<>();
        for (Category cat : all) {
            boolean isChild = (parentId == null && cat.getParentId() == null) ||
                    (parentId != null && parentId.equals(cat.getParentId()));
            if (isChild) {
                CategoryTreeNode node = new CategoryTreeNode();
                node.setId(cat.getId());
                node.setName(cat.getName());
                node.setCode(cat.getCode());
                node.setParentId(cat.getParentId());
                node.setLevel(cat.getLevel());
                node.setSortOrder(cat.getSortOrder());
                node.setDescription(cat.getDescription());

                int toolCount = toolCountMap.getOrDefault(cat.getId(), 0);
                int maintenanceCount = maintenanceCountMap.getOrDefault(cat.getId(), 0);

                List<CategoryTreeNode> children = buildTreeWithStats(
                    all, cat.getId(), toolCountMap, maintenanceCountMap, totalMaintenanceItems, defaultCycleDaysMap
                );

                for (CategoryTreeNode child : children) {
                    toolCount += child.getToolCount() != null ? child.getToolCount() : 0;
                    maintenanceCount = Math.max(maintenanceCount,
                        child.getMaintenanceItemCount() != null ? child.getMaintenanceItemCount() : 0);
                }

                Integer categoryCycle = defaultCycleDaysMap.get(cat.getId());

                node.setToolCount(toolCount);
                node.setMaintenanceItemCount(maintenanceCount);
                node.setMaintenanceItemTotal(totalMaintenanceItems);
                node.setMaintenanceCoverageRate(
                    totalMaintenanceItems > 0 ? (maintenanceCount * 100.0 / totalMaintenanceItems) : 0.0
                );
                node.setDefaultCycleDays(categoryCycle);
                node.setChildren(children);

                tree.add(node);
            }
        }
        tree.sort(Comparator.comparing(n -> n.getSortOrder() != null ? n.getSortOrder() : 0));
        return tree;
    }

    @Override
    public CategoryDeletionCheck checkDeletion(Long id) {
        CategoryDeletionCheck check = new CategoryDeletionCheck();

        long subCategoryCount = categoryRepository.countByParentId(id);
        long toolCount = toolRepository.countByCategoryId(id) + toolRepository.countBySubCategoryId(id);

        List<Tool> toolsByCategory = toolRepository.findByCategoryId(id);
        List<Tool> toolsBySubCategory = toolRepository.findBySubCategoryId(id);
        List<Tool> allTools = new ArrayList<>();
        allTools.addAll(toolsByCategory);
        allTools.addAll(toolsBySubCategory);
        List<Long> toolIds = allTools.stream().map(Tool::getId).collect(Collectors.toList());
        long maintenanceRecordCount = toolIds.isEmpty() ? 0 : maintenanceRecordRepository.countByToolIdIn(toolIds);

        check.setSubCategoryCount(subCategoryCount);
        check.setToolCount(toolCount);
        check.setMaintenanceRecordCount(maintenanceRecordCount);

        boolean canDelete = (subCategoryCount == 0 && toolCount == 0 && maintenanceRecordCount == 0);
        check.setCanDelete(canDelete);

        if (!canDelete) {
            StringBuilder msg = new StringBuilder("该分类下存在关联数据，无法删除：");
            List<String> parts = new ArrayList<>();
            if (subCategoryCount > 0) {
                parts.add(subCategoryCount + " 个子分类");
            }
            if (toolCount > 0) {
                parts.add(toolCount + " 个工具");
            }
            if (maintenanceRecordCount > 0) {
                parts.add(maintenanceRecordCount + " 条保养记录");
            }
            msg.append(String.join("、", parts));
            check.setMessage(msg.toString());
        } else {
            check.setMessage("该分类可以安全删除");
        }

        return check;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CategoryDeletionCheck check = checkDeletion(id);
        if (!check.isCanDelete()) {
            throw new RuntimeException(check.getMessage());
        }
        categoryRepository.deleteById(id);
        cacheService.evictCategoryCache();
        cacheService.cacheCategoryTree(categoryRepository.findAll());
    }
}
