package com.home.tools.dto;

import com.home.tools.entity.Tool;
import com.home.tools.entity.ToolKitItem;

public class ToolKitItemDetailDTO {

    public ToolKitItemDetailDTO() {}

    private ToolKitItem item;
    private Tool tool;
    private Boolean available;
    private String missingReason;

    public ToolKitItem getItem() { return item; }
    public void setItem(ToolKitItem item) { this.item = item; }
    public Tool getTool() { return tool; }
    public void setTool(Tool tool) { this.tool = tool; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public String getMissingReason() { return missingReason; }
    public void setMissingReason(String missingReason) { this.missingReason = missingReason; }
}
