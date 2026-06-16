package com.home.tools.dto;

import java.util.List;

public class ScenarioAnalysisDTO {

    private String scenario;
    private Integer totalMinutes;
    private Integer usageCount;
    private String lastUseDate;
    private List<ScenarioToolDTO> topTools;

    public ScenarioAnalysisDTO() {}

    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }
    public Integer getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(Integer totalMinutes) { this.totalMinutes = totalMinutes; }
    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    public String getLastUseDate() { return lastUseDate; }
    public void setLastUseDate(String lastUseDate) { this.lastUseDate = lastUseDate; }
    public List<ScenarioToolDTO> getTopTools() { return topTools; }
    public void setTopTools(List<ScenarioToolDTO> topTools) { this.topTools = topTools; }
}
