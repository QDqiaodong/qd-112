package com.home.tools.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    public InventoryItem() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inventoryId;

    @Column(nullable = false)
    private Long toolId;

    private String expectedStatus;

    private String actualStatus;

    private Boolean checked;

    private String remarks;

    private String toolName;

    private String toolModel;

    private String toolBrand;

    private Long categoryId;

    private String categoryName;

    private String location;

    private String snapshotStatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public String getExpectedStatus() { return expectedStatus; }
    public void setExpectedStatus(String expectedStatus) { this.expectedStatus = expectedStatus; }
    public String getActualStatus() { return actualStatus; }
    public void setActualStatus(String actualStatus) { this.actualStatus = actualStatus; }
    public Boolean getChecked() { return checked; }
    public void setChecked(Boolean checked) { this.checked = checked; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getToolModel() { return toolModel; }
    public void setToolModel(String toolModel) { this.toolModel = toolModel; }
    public String getToolBrand() { return toolBrand; }
    public void setToolBrand(String toolBrand) { this.toolBrand = toolBrand; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSnapshotStatus() { return snapshotStatus; }
    public void setSnapshotStatus(String snapshotStatus) { this.snapshotStatus = snapshotStatus; }
}
