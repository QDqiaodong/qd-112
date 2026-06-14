package com.home.tools.service.impl;

import com.home.tools.entity.Category;
import com.home.tools.entity.MaintenanceItem;
import com.home.tools.repository.CategoryRepository;
import com.home.tools.repository.MaintenanceItemRepository;
import com.home.tools.service.CacheService;
import com.home.tools.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final MaintenanceItemRepository maintenanceItemRepository;
    private final CacheService cacheService;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               MaintenanceItemRepository maintenanceItemRepository,
                               CacheService cacheService) {
        this.categoryRepository = categoryRepository;
        this.maintenanceItemRepository = maintenanceItemRepository;
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
}
