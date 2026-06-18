package com.home.tools.dto;

import com.home.tools.entity.ToolKit;
import java.util.List;

public class ToolKitWithItemsDTO {

    public ToolKitWithItemsDTO() {}

    private ToolKit kit;
    private List<ToolKitItemDetailDTO> items;
    private Integer totalItems;
    private Integer availableItems;
    private Integer missingItems;

    public ToolKit getKit() { return kit; }
    public void setKit(ToolKit kit) { this.kit = kit; }
    public List<ToolKitItemDetailDTO> getItems() { return items; }
    public void setItems(List<ToolKitItemDetailDTO> items) { this.items = items; }
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    public Integer getAvailableItems() { return availableItems; }
    public void setAvailableItems(Integer availableItems) { this.availableItems = availableItems; }
    public Integer getMissingItems() { return missingItems; }
    public void setMissingItems(Integer missingItems) { this.missingItems = missingItems; }
}
