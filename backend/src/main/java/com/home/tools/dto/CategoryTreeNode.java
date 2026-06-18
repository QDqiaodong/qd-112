package com.home.tools.dto;

import java.util.List;

public class CategoryTreeNode {

    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private String description;
    private Integer toolCount;
    private Integer maintenanceItemCount;
    private Integer maintenanceItemTotal;
    private Double maintenanceCoverageRate;
    private Integer defaultCycleDays;
    private List<CategoryTreeNode> children;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getToolCount() { return toolCount; }
    public void setToolCount(Integer toolCount) { this.toolCount = toolCount; }
    public Integer getMaintenanceItemCount() { return maintenanceItemCount; }
    public void setMaintenanceItemCount(Integer maintenanceItemCount) { this.maintenanceItemCount = maintenanceItemCount; }
    public Integer getMaintenanceItemTotal() { return maintenanceItemTotal; }
    public void setMaintenanceItemTotal(Integer maintenanceItemTotal) { this.maintenanceItemTotal = maintenanceItemTotal; }
    public Double getMaintenanceCoverageRate() { return maintenanceCoverageRate; }
    public void setMaintenanceCoverageRate(Double maintenanceCoverageRate) { this.maintenanceCoverageRate = maintenanceCoverageRate; }
    public Integer getDefaultCycleDays() { return defaultCycleDays; }
    public void setDefaultCycleDays(Integer defaultCycleDays) { this.defaultCycleDays = defaultCycleDays; }
    public List<CategoryTreeNode> getChildren() { return children; }
    public void setChildren(List<CategoryTreeNode> children) { this.children = children; }
}
