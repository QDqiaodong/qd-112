package com.home.tools.dto;

import java.util.List;

public class DifferenceGroupDTO {

    public DifferenceGroupDTO() {}

    private String groupKey;
    private String groupType;
    private List<DifferenceItemDTO> items;

    private Integer totalCount;
    private Integer checkedCount;
    private Integer uncheckedCount;
    private Integer mismatchedCount;
    private Integer lostCount;
    private Double completionPercent;

    public String getGroupKey() { return groupKey; }
    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }
    public String getGroupType() { return groupType; }
    public void setGroupType(String groupType) { this.groupType = groupType; }
    public List<DifferenceItemDTO> getItems() { return items; }
    public void setItems(List<DifferenceItemDTO> items) { this.items = items; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getCheckedCount() { return checkedCount; }
    public void setCheckedCount(Integer checkedCount) { this.checkedCount = checkedCount; }
    public Integer getUncheckedCount() { return uncheckedCount; }
    public void setUncheckedCount(Integer uncheckedCount) { this.uncheckedCount = uncheckedCount; }
    public Integer getMismatchedCount() { return mismatchedCount; }
    public void setMismatchedCount(Integer mismatchedCount) { this.mismatchedCount = mismatchedCount; }
    public Integer getLostCount() { return lostCount; }
    public void setLostCount(Integer lostCount) { this.lostCount = lostCount; }
    public Double getCompletionPercent() { return completionPercent; }
    public void setCompletionPercent(Double completionPercent) { this.completionPercent = completionPercent; }
}
