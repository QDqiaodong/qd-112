package com.home.tools.dto;

import java.util.List;

public class CategoryMaintenanceInheritanceResult {

    private Long categoryId;
    private String categoryName;
    private String categoryCode;
    private Long parentCategoryId;
    private String parentCategoryName;
    private Integer level;

    private List<MaintenanceItemWithSource> allItems;

    private List<MaintenanceItemWithSource> overriddenItems;

    private List<MaintenanceItemWithSource> inheritedItems;

    private List<MaintenanceItemWithSource> missingItems;

    private List<MaintenanceItemWithSource> disabledItems;

    private int totalCount;
    private int overriddenCount;
    private int inheritedCount;
    private int missingCount;
    private int disabledCount;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public Long getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public String getParentCategoryName() {
        return parentCategoryName;
    }

    public void setParentCategoryName(String parentCategoryName) {
        this.parentCategoryName = parentCategoryName;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public List<MaintenanceItemWithSource> getAllItems() {
        return allItems;
    }

    public void setAllItems(List<MaintenanceItemWithSource> allItems) {
        this.allItems = allItems;
    }

    public List<MaintenanceItemWithSource> getOverriddenItems() {
        return overriddenItems;
    }

    public void setOverriddenItems(List<MaintenanceItemWithSource> overriddenItems) {
        this.overriddenItems = overriddenItems;
    }

    public List<MaintenanceItemWithSource> getInheritedItems() {
        return inheritedItems;
    }

    public void setInheritedItems(List<MaintenanceItemWithSource> inheritedItems) {
        this.inheritedItems = inheritedItems;
    }

    public List<MaintenanceItemWithSource> getMissingItems() {
        return missingItems;
    }

    public void setMissingItems(List<MaintenanceItemWithSource> missingItems) {
        this.missingItems = missingItems;
    }

    public List<MaintenanceItemWithSource> getDisabledItems() {
        return disabledItems;
    }

    public void setDisabledItems(List<MaintenanceItemWithSource> disabledItems) {
        this.disabledItems = disabledItems;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getOverriddenCount() {
        return overriddenCount;
    }

    public void setOverriddenCount(int overriddenCount) {
        this.overriddenCount = overriddenCount;
    }

    public int getInheritedCount() {
        return inheritedCount;
    }

    public void setInheritedCount(int inheritedCount) {
        this.inheritedCount = inheritedCount;
    }

    public int getMissingCount() {
        return missingCount;
    }

    public void setMissingCount(int missingCount) {
        this.missingCount = missingCount;
    }

    public int getDisabledCount() {
        return disabledCount;
    }

    public void setDisabledCount(int disabledCount) {
        this.disabledCount = disabledCount;
    }
}
