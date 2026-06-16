package com.home.tools.dto;

import java.time.LocalDate;

public class ToolAvailabilityScore {

    private Long toolId;
    private Integer totalScore;
    private String grade;

    private Integer statusScore;
    private String statusDetail;

    private Integer maintenanceScore;
    private String maintenanceDetail;
    private LocalDate nextMaintenanceDate;
    private Integer overdueDays;

    private Integer usageScore;
    private String usageDetail;
    private Integer usageCount30Days;
    private Integer usageCount90Days;
    private LocalDate lastUseDate;

    private Integer inventoryScore;
    private String inventoryDetail;
    private LocalDate lastInventoryDate;
    private Boolean lastInventoryChecked;
    private String lastInventoryActualStatus;
    private String lastInventoryExpectedStatus;

    public ToolAvailabilityScore() {}

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
        this.grade = calculateGrade(totalScore);
    }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public Integer getStatusScore() { return statusScore; }
    public void setStatusScore(Integer statusScore) { this.statusScore = statusScore; }

    public String getStatusDetail() { return statusDetail; }
    public void setStatusDetail(String statusDetail) { this.statusDetail = statusDetail; }

    public Integer getMaintenanceScore() { return maintenanceScore; }
    public void setMaintenanceScore(Integer maintenanceScore) { this.maintenanceScore = maintenanceScore; }

    public String getMaintenanceDetail() { return maintenanceDetail; }
    public void setMaintenanceDetail(String maintenanceDetail) { this.maintenanceDetail = maintenanceDetail; }

    public LocalDate getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDate nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }

    public Integer getOverdueDays() { return overdueDays; }
    public void setOverdueDays(Integer overdueDays) { this.overdueDays = overdueDays; }

    public Integer getUsageScore() { return usageScore; }
    public void setUsageScore(Integer usageScore) { this.usageScore = usageScore; }

    public String getUsageDetail() { return usageDetail; }
    public void setUsageDetail(String usageDetail) { this.usageDetail = usageDetail; }

    public Integer getUsageCount30Days() { return usageCount30Days; }
    public void setUsageCount30Days(Integer usageCount30Days) { this.usageCount30Days = usageCount30Days; }

    public Integer getUsageCount90Days() { return usageCount90Days; }
    public void setUsageCount90Days(Integer usageCount90Days) { this.usageCount90Days = usageCount90Days; }

    public LocalDate getLastUseDate() { return lastUseDate; }
    public void setLastUseDate(LocalDate lastUseDate) { this.lastUseDate = lastUseDate; }

    public Integer getInventoryScore() { return inventoryScore; }
    public void setInventoryScore(Integer inventoryScore) { this.inventoryScore = inventoryScore; }

    public String getInventoryDetail() { return inventoryDetail; }
    public void setInventoryDetail(String inventoryDetail) { this.inventoryDetail = inventoryDetail; }

    public LocalDate getLastInventoryDate() { return lastInventoryDate; }
    public void setLastInventoryDate(LocalDate lastInventoryDate) { this.lastInventoryDate = lastInventoryDate; }

    public Boolean getLastInventoryChecked() { return lastInventoryChecked; }
    public void setLastInventoryChecked(Boolean lastInventoryChecked) { this.lastInventoryChecked = lastInventoryChecked; }

    public String getLastInventoryActualStatus() { return lastInventoryActualStatus; }
    public void setLastInventoryActualStatus(String lastInventoryActualStatus) { this.lastInventoryActualStatus = lastInventoryActualStatus; }

    public String getLastInventoryExpectedStatus() { return lastInventoryExpectedStatus; }
    public void setLastInventoryExpectedStatus(String lastInventoryExpectedStatus) { this.lastInventoryExpectedStatus = lastInventoryExpectedStatus; }

    private String calculateGrade(Integer score) {
        if (score == null) return "-";
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "E";
    }
}
