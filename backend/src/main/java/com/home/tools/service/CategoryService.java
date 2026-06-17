package com.home.tools.service;

import com.home.tools.dto.CategoryDeletionCheck;
import com.home.tools.dto.CategoryTreeNode;
import com.home.tools.entity.Category;
import com.home.tools.entity.MaintenanceItem;
import java.util.List;
import java.util.Map;

public interface CategoryService {

    List<Map<String, Object>> getCategoryTree();

    List<CategoryTreeNode> getCategoryTreeWithStats();

    List<MaintenanceItem> listMaintenanceItems();

    Category getById(Long id);

    Category create(Category category);

    Category update(Long id, Category category);

    void updateSortOrder(Long id, Integer sortOrder);

    CategoryDeletionCheck checkDeletion(Long id);

    void delete(Long id);
}
