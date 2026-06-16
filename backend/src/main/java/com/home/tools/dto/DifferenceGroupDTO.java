package com.home.tools.dto;

import java.util.List;

public class DifferenceGroupDTO {

    public DifferenceGroupDTO() {}

    private String groupKey;
    private String groupType;
    private List<DifferenceItemDTO> items;

    public String getGroupKey() { return groupKey; }
    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }
    public String getGroupType() { return groupType; }
    public void setGroupType(String groupType) { this.groupType = groupType; }
    public List<DifferenceItemDTO> getItems() { return items; }
    public void setItems(List<DifferenceItemDTO> items) { this.items = items; }
}
