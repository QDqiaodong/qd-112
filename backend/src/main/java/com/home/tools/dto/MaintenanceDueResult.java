package com.home.tools.dto;

import com.home.tools.entity.MaintenanceDueStatus;
import java.time.LocalDate;

public class MaintenanceDueResult {

    private Long toolId;
    private String toolName;
    private Integer cycleDays;
    private Integer effectiveCycleDays;
    private String cycleSource;
    private LocalDate lastMaintenanceDate;
    private LocalDate calculatedDueDate;
    private LocalDate storedDueDate;
    private MaintenanceDueStatus status;
    private String statusDescription;
    private Integer daysRemaining;
    private Integer daysOverdue;

    public Long getToolId() {
        return toolId;
    }

    public void setToolId(Long toolId) {
        this.toolId = toolId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Integer getCycleDays() {
        return cycleDays;
    }

    public void setCycleDays(Integer cycleDays) {
        this.cycleDays = cycleDays;
    }

    public Integer getEffectiveCycleDays() {
        return effectiveCycleDays;
    }

    public void setEffectiveCycleDays(Integer effectiveCycleDays) {
        this.effectiveCycleDays = effectiveCycleDays;
    }

    public String getCycleSource() {
        return cycleSource;
    }

    public void setCycleSource(String cycleSource) {
        this.cycleSource = cycleSource;
    }

    public LocalDate getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public LocalDate getCalculatedDueDate() {
        return calculatedDueDate;
    }

    public void setCalculatedDueDate(LocalDate calculatedDueDate) {
        this.calculatedDueDate = calculatedDueDate;
    }

    public LocalDate getStoredDueDate() {
        return storedDueDate;
    }

    public void setStoredDueDate(LocalDate storedDueDate) {
        this.storedDueDate = storedDueDate;
    }

    public MaintenanceDueStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceDueStatus status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public Integer getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Integer daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public Integer getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(Integer daysOverdue) {
        this.daysOverdue = daysOverdue;
    }
}
