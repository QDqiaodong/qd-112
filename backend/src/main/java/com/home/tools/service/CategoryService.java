package com.home.tools.service;

import com.home.tools.entity.Category;
import com.home.tools.entity.MaintenanceItem;
import java.util.List;
import java.util.Map;

public interface CategoryService {

    List<Map<String, Object>> getCategoryTree();

    List<MaintenanceItem> listMaintenanceItems();
}
