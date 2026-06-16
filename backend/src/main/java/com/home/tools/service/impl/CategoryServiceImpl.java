package com.home.tools.service.impl;

import com.home.tools.dto.CategoryDeletionCheck;
import com.home.tools.entity.Category;
import com.home.tools.entity.MaintenanceItem;
import com.home.tools.entity.Tool;
import com.home.tools.repository.CategoryRepository;
import com.home.tools.repository.MaintenanceItemRepository;
import com.home.tools.repository.MaintenanceRecordRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.CacheService;
import com.home.tools.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final CacheService cacheService;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               MaintenanceItemRepository maintenanceItemRepository,
                               ToolRepository toolRepository,
                               MaintenanceRecordRepository maintenanceRecordRepository,
                               CacheService cacheService) {
        this.categoryRepository = categoryRepository;
        this.maintenanceItemRepository = maintenanceItemRepository;
        this.toolRepository = toolRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
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

    @Override
    public CategoryDeletionCheck checkDeletion(Long id) {
        CategoryDeletionCheck check = new CategoryDeletionCheck();

        long subCategoryCount = categoryRepository.countByParentId(id);
        long toolCount = toolRepository.countByCategoryId(id) + toolRepository.countBySubCategoryId(id);

        List<Tool> tools = toolRepository.findByCategoryId(id);
        List<Long> toolIds = tools.stream().map(Tool::getId).collect(Collectors.toList());
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
