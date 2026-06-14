package com.home.tools.service;

import com.home.tools.entity.Category;
import java.util.List;
import java.util.Map;

public interface CacheService {

    void cacheCategoryTree(List<Category> categories);

    List<Map<String, Object>> getCategoryTreeFromCache();

    void cacheMaintenanceCycles(Map<String, String> cycles);

    Map<String, String> getMaintenanceCyclesFromCache();

    void cacheStatsOverview(String json);

    String getStatsOverviewFromCache();

    void evictCategoryCache();

    void evictStatsCache();
}
