package com.home.tools.service.impl;

import com.home.tools.entity.Category;
import com.home.tools.service.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    private static final String CATEGORY_TREE_KEY = "category:tree";
    private static final String MAINTENANCE_CYCLES_KEY = "maintenance:cycles";
    private static final String STATS_OVERVIEW_KEY = "stats:overview";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheServiceImpl(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void cacheCategoryTree(List<Category> categories) {
        try {
            for (Category cat : categories) {
                String json = objectMapper.writeValueAsString(cat);
                redisTemplate.opsForZSet().add(CATEGORY_TREE_KEY, json, cat.getSortOrder() != null ? cat.getSortOrder() : 0);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Map<String, Object>> getCategoryTreeFromCache() {
        Set<Object> set = redisTemplate.opsForZSet().range(CATEGORY_TREE_KEY, 0, -1);
        if (set == null || set.isEmpty()) {
            return null;
        }
        List<Category> categories = new ArrayList<>();
        for (Object obj : set) {
            try {
                String json = obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
                categories.add(objectMapper.readValue(json, Category.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return buildTree(categories, null);
    }

    @Override
    public void cacheMaintenanceCycles(Map<String, String> cycles) {
        redisTemplate.opsForHash().putAll(MAINTENANCE_CYCLES_KEY, cycles);
    }

    @Override
    public Map<String, String> getMaintenanceCyclesFromCache() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(MAINTENANCE_CYCLES_KEY);
        if (entries.isEmpty()) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        entries.forEach((k, v) -> result.put(k.toString(), v.toString()));
        return result;
    }

    @Override
    public void cacheStatsOverview(String json) {
        redisTemplate.opsForValue().set(STATS_OVERVIEW_KEY, json, 10, TimeUnit.MINUTES);
    }

    @Override
    public String getStatsOverviewFromCache() {
        Object obj = redisTemplate.opsForValue().get(STATS_OVERVIEW_KEY);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    @Override
    public void evictCategoryCache() {
        redisTemplate.delete(CATEGORY_TREE_KEY);
    }

    @Override
    public void evictStatsCache() {
        redisTemplate.delete(STATS_OVERVIEW_KEY);
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
