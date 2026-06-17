package com.home.tools.dto;

public class InventoryProgressDTO {

    public InventoryProgressDTO() {}

    private Integer totalTools;

    private Integer checkedCount;

    private Integer uncheckedCount;

    private Integer mismatchedCount;

    private Integer lostCount;

    private Double checkedPercent;

    private Double uncheckedPercent;

    private Double mismatchedPercent;

    private Double lostPercent;

    public Integer getTotalTools() { return totalTools; }
    public void setTotalTools(Integer totalTools) { this.totalTools = totalTools; }
    public Integer getCheckedCount() { return checkedCount; }
    public void setCheckedCount(Integer checkedCount) { this.checkedCount = checkedCount; }
    public Integer getUncheckedCount() { return uncheckedCount; }
    public void setUncheckedCount(Integer uncheckedCount) { this.uncheckedCount = uncheckedCount; }
    public Integer getMismatchedCount() { return mismatchedCount; }
    public void setMismatchedCount(Integer mismatchedCount) { this.mismatchedCount = mismatchedCount; }
    public Integer getLostCount() { return lostCount; }
    public void setLostCount(Integer lostCount) { this.lostCount = lostCount; }
    public Double getCheckedPercent() { return checkedPercent; }
    public void setCheckedPercent(Double checkedPercent) { this.checkedPercent = checkedPercent; }
    public Double getUncheckedPercent() { return uncheckedPercent; }
    public void setUncheckedPercent(Double uncheckedPercent) { this.uncheckedPercent = uncheckedPercent; }
    public Double getMismatchedPercent() { return mismatchedPercent; }
    public void setMismatchedPercent(Double mismatchedPercent) { this.mismatchedPercent = mismatchedPercent; }
    public Double getLostPercent() { return lostPercent; }
    public void setLostPercent(Double lostPercent) { this.lostPercent = lostPercent; }
}
