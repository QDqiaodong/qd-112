package com.home.tools.dto;

import java.math.BigDecimal;

public class CostSummaryDTO {

    public CostSummaryDTO() {}

    private String groupKey;
    private String groupType;
    private BigDecimal purchaseCost;
    private BigDecimal maintenanceCost;
    private BigDecimal partReplacementCost;
    private BigDecimal totalCost;
    private Long toolCount;

    public String getGroupKey() { return groupKey; }
    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }
    public String getGroupType() { return groupType; }
    public void setGroupType(String groupType) { this.groupType = groupType; }
    public BigDecimal getPurchaseCost() { return purchaseCost; }
    public void setPurchaseCost(BigDecimal purchaseCost) { this.purchaseCost = purchaseCost; }
    public BigDecimal getMaintenanceCost() { return maintenanceCost; }
    public void setMaintenanceCost(BigDecimal maintenanceCost) { this.maintenanceCost = maintenanceCost; }
    public BigDecimal getPartReplacementCost() { return partReplacementCost; }
    public void setPartReplacementCost(BigDecimal partReplacementCost) { this.partReplacementCost = partReplacementCost; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public Long getToolCount() { return toolCount; }
    public void setToolCount(Long toolCount) { this.toolCount = toolCount; }
}
