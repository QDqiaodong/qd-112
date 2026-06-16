package com.home.tools.dto;

public class ScenarioToolDTO {

    private Long toolId;
    private String toolName;
    private Integer totalMinutes;
    private Integer usageCount;
    private String lastUseDate;

    public ScenarioToolDTO() {}

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public Integer getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(Integer totalMinutes) { this.totalMinutes = totalMinutes; }
    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    public String getLastUseDate() { return lastUseDate; }
    public void setLastUseDate(String lastUseDate) { this.lastUseDate = lastUseDate; }
}
