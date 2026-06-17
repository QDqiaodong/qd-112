package com.home.tools.service.impl;

import com.home.tools.dto.CategoryMaintenanceInheritanceResult;
import com.home.tools.dto.MaintenanceItemWithSource;
import com.home.tools.entity.Category;
import com.home.tools.entity.CategoryMaintenanceItem;
import com.home.tools.entity.MaintenanceItem;
import com.home.tools.entity.Tool;
import com.home.tools.repository.CategoryMaintenanceItemRepository;
import com.home.tools.repository.CategoryRepository;
import com.home.tools.repository.MaintenanceItemRepository;
import com.home.tools.repository.ToolRepository;
import com.home.tools.service.CacheService;
import com.home.tools.service.CategoryMaintenanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryMaintenanceServiceImpl implements CategoryMaintenanceService {

    private final CategoryMaintenanceItemRepository categoryMaintenanceItemRepository;
    private final CategoryRepository categoryRepository;
    private final MaintenanceItemRepository maintenanceItemRepository;
    private final ToolRepository toolRepository;
    private final CacheService cacheService;

    public CategoryMaintenanceServiceImpl(CategoryMaintenanceItemRepository categoryMaintenanceItemRepository,
                                          CategoryRepository categoryRepository,
                                          MaintenanceItemRepository maintenanceItemRepository,
                                          ToolRepository toolRepository,
                                          CacheService cacheService) {
        this.categoryMaintenanceItemRepository = categoryMaintenanceItemRepository;
        this.categoryRepository = categoryRepository;
        this.maintenanceItemRepository = maintenanceItemRepository;
        this.toolRepository = toolRepository;
        this.cacheService = cacheService;
    }

    @Override
    public CategoryMaintenanceInheritanceResult getInheritanceResult(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));

        CategoryMaintenanceInheritanceResult result = new CategoryMaintenanceInheritanceResult();
        result.setCategoryId(category.getId());
        result.setCategoryName(category.getName());
        result.setCategoryCode(category.getCode());
        result.setLevel(category.getLevel());
        result.setParentCategoryId(category.getParentId());

        if (category.getParentId() != null) {
            Category parent = categoryRepository.findById(category.getParentId()).orElse(null);
            if (parent != null) {
                result.setParentCategoryName(parent.getName());
            }
        }

        Map<String, MaintenanceItem> allMaintenanceItems = maintenanceItemRepository.findAll()
                .stream()
                .collect(Collectors.toMap(MaintenanceItem::getCode, item -> item, (a, b) -> a));

        List<CategoryMaintenanceItem> parentItems = new ArrayList<>();
        List<Long> ancestorChain = getAncestorChain(categoryId);
        for (Long ancestorId : ancestorChain) {
            parentItems.addAll(categoryMaintenanceItemRepository.findByCategoryId(ancestorId));
        }

        Map<String, CategoryMaintenanceItem> parentItemMap = new LinkedHashMap<>();
        Map<String, Long> parentItemSource = new LinkedHashMap<>();
        for (int i = ancestorChain.size() - 1; i >= 0; i--) {
            Long ancestorId = ancestorChain.get(i);
            List<CategoryMaintenanceItem> ancestorItems = categoryMaintenanceItemRepository.findByCategoryId(ancestorId);
            for (CategoryMaintenanceItem item : ancestorItems) {
                if (item.getEnabled() != null && !item.getEnabled()) {
                    parentItemMap.remove(item.getMaintenanceItemCode());
                    parentItemSource.remove(item.getMaintenanceItemCode());
                } else {
                    parentItemMap.put(item.getMaintenanceItemCode(), item);
                    parentItemSource.put(item.getMaintenanceItemCode(), ancestorId);
                }
            }
        }

        List<CategoryMaintenanceItem> currentItems = categoryMaintenanceItemRepository.findByCategoryId(categoryId);
        Map<String, CategoryMaintenanceItem> currentItemMap = currentItems.stream()
                .collect(Collectors.toMap(
                        CategoryMaintenanceItem::getMaintenanceItemCode,
                        item -> item,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Set<String> allCodes = new LinkedHashSet<>();
        allCodes.addAll(parentItemMap.keySet());
        allCodes.addAll(currentItemMap.keySet());
        allCodes.addAll(allMaintenanceItems.keySet());

        List<MaintenanceItemWithSource> overriddenItems = new ArrayList<>();
        List<MaintenanceItemWithSource> inheritedItems = new ArrayList<>();
        List<MaintenanceItemWithSource> missingItems = new ArrayList<>();
        List<MaintenanceItemWithSource> disabledItems = new ArrayList<>();
        List<MaintenanceItemWithSource> allItems = new ArrayList<>();

        for (String code : allCodes) {
            MaintenanceItem baseItem = allMaintenanceItems.get(code);
            CategoryMaintenanceItem current = currentItemMap.get(code);
            CategoryMaintenanceItem parent = parentItemMap.get(code);
            Long parentSourceId = parentItemSource.get(code);

            boolean hasInParent = parent != null;
            boolean hasInCurrent = current != null;

            MaintenanceItemWithSource wrapper = new MaintenanceItemWithSource();
            wrapper.setItem(baseItem);
            wrapper.setEnabled(true);

            if (hasInCurrent) {
                if (hasInParent) {
                    wrapper.setSourceType("OVERRIDDEN");
                    wrapper.setOverridden(true);
                    if (parentSourceId != null) {
                        Category srcCat = categoryRepository.findById(parentSourceId).orElse(null);
                        wrapper.setSourceCategoryId(String.valueOf(parentSourceId));
                        wrapper.setSourceCategoryName(srcCat != null ? srcCat.getName() : "上级分类");
                    }
                    if (current.getEnabled() != null && !current.getEnabled()) {
                        wrapper.setEnabled(false);
                        wrapper.setSourceType("DISABLED");
                    }
                } else {
                    wrapper.setSourceType("OVERRIDDEN");
                    wrapper.setOverridden(true);
                    wrapper.setSourceCategoryId(String.valueOf(categoryId));
                    wrapper.setSourceCategoryName(category.getName());
                    if (current.getEnabled() != null && !current.getEnabled()) {
                        wrapper.setEnabled(false);
                        wrapper.setSourceType("DISABLED");
                    }
                }

                if (current.getCustomCycleDays() != null) {
                    wrapper.setEffectiveCycleDays(current.getCustomCycleDays());
                    wrapper.setCycleSource("分类自定义");
                    wrapper.setOverriddenCycleDays(current.getCustomCycleDays());
                } else if (baseItem != null) {
                    wrapper.setEffectiveCycleDays(baseItem.getDefaultCycleDays());
                    wrapper.setCycleSource("保养项默认");
                }
                wrapper.setRemarks(current.getRemarks());

            } else if (hasInParent) {
                wrapper.setSourceType("INHERITED");
                wrapper.setOverridden(false);
                if (parentSourceId != null) {
                    Category srcCat = categoryRepository.findById(parentSourceId).orElse(null);
                    wrapper.setSourceCategoryId(String.valueOf(parentSourceId));
                    wrapper.setSourceCategoryName(srcCat != null ? srcCat.getName() : "上级分类");
                }
                if (parent.getCustomCycleDays() != null) {
                    wrapper.setEffectiveCycleDays(parent.getCustomCycleDays());
                    wrapper.setCycleSource("继承自定义周期");
                } else if (baseItem != null) {
                    wrapper.setEffectiveCycleDays(baseItem.getDefaultCycleDays());
                    wrapper.setCycleSource("继承默认周期");
                }
                wrapper.setRemarks(parent.getRemarks());

            } else {
                wrapper.setSourceType("MISSING");
                wrapper.setOverridden(false);
                if (baseItem != null) {
                    wrapper.setEffectiveCycleDays(baseItem.getDefaultCycleDays());
                    wrapper.setCycleSource("保养项默认（未配置）");
                }
            }

            if (baseItem == null) {
                continue;
            }

            allItems.add(wrapper);

            String sourceType = wrapper.getSourceType();
            if ("DISABLED".equals(sourceType)) {
                disabledItems.add(wrapper);
            } else if ("OVERRIDDEN".equals(sourceType)) {
                overriddenItems.add(wrapper);
            } else if ("INHERITED".equals(sourceType)) {
                inheritedItems.add(wrapper);
            } else if ("MISSING".equals(sourceType)) {
                missingItems.add(wrapper);
            }
        }

        allItems.sort(Comparator.comparing(w -> {
            MaintenanceItem item = w.getItem();
            return item != null ? item.getCode() : "";
        }));

        result.setAllItems(allItems);
        result.setOverriddenItems(overriddenItems);
        result.setInheritedItems(inheritedItems);
        result.setMissingItems(missingItems);
        result.setDisabledItems(disabledItems);

        result.setTotalCount(allItems.size());
        result.setOverriddenCount(overriddenItems.size());
        result.setInheritedCount(inheritedItems.size());
        result.setMissingCount(missingItems.size());
        result.setDisabledCount(disabledItems.size());

        return result;
    }

    @Override
    public List<MaintenanceItemWithSource> getEffectiveItems(Long categoryId) {
        CategoryMaintenanceInheritanceResult result = getInheritanceResult(categoryId);
        List<MaintenanceItemWithSource> effective = new ArrayList<>();
        for (MaintenanceItemWithSource item : result.getAllItems()) {
            if (Boolean.TRUE.equals(item.getEnabled())) {
                effective.add(item);
            }
        }
        return effective;
    }

    @Override
    public List<MaintenanceItemWithSource> getEffectiveItemsForTool(Long toolId) {
        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> new RuntimeException("Tool not found: " + toolId));
        Long categoryId = tool.getSubCategoryId() != null ? tool.getSubCategoryId() : tool.getCategoryId();
        if (categoryId == null) {
            return Collections.emptyList();
        }
        return getEffectiveItems(categoryId);
    }

    @Override
    @Transactional
    public CategoryMaintenanceItem saveCategoryItem(CategoryMaintenanceItem item) {
        if (item.getId() != null) {
            CategoryMaintenanceItem existing = categoryMaintenanceItemRepository.findById(item.getId())
                    .orElseThrow(() -> new RuntimeException("Item not found: " + item.getId()));
            if (item.getCustomCycleDays() != null) {
                existing.setCustomCycleDays(item.getCustomCycleDays());
            }
            if (item.getEnabled() != null) {
                existing.setEnabled(item.getEnabled());
            }
            if (item.getRemarks() != null) {
                existing.setRemarks(item.getRemarks());
            }
            return categoryMaintenanceItemRepository.save(existing);
        } else {
            Optional<CategoryMaintenanceItem> existing = categoryMaintenanceItemRepository
                    .findByCategoryIdAndMaintenanceItemCode(item.getCategoryId(), item.getMaintenanceItemCode());
            if (existing.isPresent()) {
                CategoryMaintenanceItem e = existing.get();
                if (item.getCustomCycleDays() != null) {
                    e.setCustomCycleDays(item.getCustomCycleDays());
                }
                if (item.getEnabled() != null) {
                    e.setEnabled(item.getEnabled());
                }
                if (item.getRemarks() != null) {
                    e.setRemarks(item.getRemarks());
                }
                return categoryMaintenanceItemRepository.save(e);
            }
            return categoryMaintenanceItemRepository.save(item);
        }
    }

    @Override
    @Transactional
    public void deleteCategoryItem(Long id) {
        categoryMaintenanceItemRepository.deleteById(id);
    }

    @Override
    public List<CategoryMaintenanceItem> getCategoryItems(Long categoryId) {
        return categoryMaintenanceItemRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional
    public void batchSaveCategoryItems(Long categoryId, List<CategoryMaintenanceItem> items) {
        for (CategoryMaintenanceItem item : items) {
            if (item.getCategoryId() == null) {
                item.setCategoryId(categoryId);
            }
            saveCategoryItem(item);
        }
    }

    @Override
    @Transactional
    public void inheritFromParent(Long categoryId, boolean overrideExisting) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
        if (category.getParentId() == null) {
            return;
        }

        List<CategoryMaintenanceItem> parentItems = categoryMaintenanceItemRepository
                .findByCategoryId(category.getParentId());
        List<CategoryMaintenanceItem> currentItems = categoryMaintenanceItemRepository.findByCategoryId(categoryId);
        Set<String> currentCodes = currentItems.stream()
                .map(CategoryMaintenanceItem::getMaintenanceItemCode)
                .collect(Collectors.toSet());

        for (CategoryMaintenanceItem parentItem : parentItems) {
            boolean exists = currentCodes.contains(parentItem.getMaintenanceItemCode());
            if (exists && !overrideExisting) {
                continue;
            }

            CategoryMaintenanceItem newItem = new CategoryMaintenanceItem();
            newItem.setCategoryId(categoryId);
            newItem.setMaintenanceItemCode(parentItem.getMaintenanceItemCode());
            newItem.setCustomCycleDays(parentItem.getCustomCycleDays());
            newItem.setEnabled(parentItem.getEnabled());
            newItem.setRemarks("继承自父分类" + (parentItem.getRemarks() != null ? "：" + parentItem.getRemarks() : ""));

            if (exists && overrideExisting) {
                Optional<CategoryMaintenanceItem> existing = categoryMaintenanceItemRepository
                        .findByCategoryIdAndMaintenanceItemCode(categoryId, parentItem.getMaintenanceItemCode());
                if (existing.isPresent()) {
                    CategoryMaintenanceItem e = existing.get();
                    e.setCustomCycleDays(newItem.getCustomCycleDays());
                    e.setEnabled(newItem.getEnabled());
                    e.setRemarks(newItem.getRemarks());
                    categoryMaintenanceItemRepository.save(e);
                    continue;
                }
            }

            categoryMaintenanceItemRepository.save(newItem);
        }
    }

    private List<Long> getAncestorChain(Long categoryId) {
        List<Long> chain = new ArrayList<>();
        Map<Long, Category> allMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        Long current = categoryId;
        while (current != null) {
            Category cat = allMap.get(current);
            if (cat == null) break;
            if (!cat.getId().equals(categoryId)) {
                chain.add(cat.getId());
            }
            current = cat.getParentId();
        }
        Collections.reverse(chain);
        return chain;
    }
}
