package com.home.tools.dto;

public class DifferenceItemDTO {

    public DifferenceItemDTO() {}

    private Long toolId;
    private String toolName;
    private String toolModel;
    private String toolBrand;
    private String expectedStatus;
    private String actualStatus;
    private Boolean checked;
    private String categoryName;
    private String location;
    private String differenceType;
    private String remarks;

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getToolModel() { return toolModel; }
    public void setToolModel(String toolModel) { this.toolModel = toolModel; }
    public String getToolBrand() { return toolBrand; }
    public void setToolBrand(String toolBrand) { this.toolBrand = toolBrand; }
    public String getExpectedStatus() { return expectedStatus; }
    public void setExpectedStatus(String expectedStatus) { this.expectedStatus = expectedStatus; }
    public String getActualStatus() { return actualStatus; }
    public void setActualStatus(String actualStatus) { this.actualStatus = actualStatus; }
    public Boolean getChecked() { return checked; }
    public void setChecked(Boolean checked) { this.checked = checked; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDifferenceType() { return differenceType; }
    public void setDifferenceType(String differenceType) { this.differenceType = differenceType; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
