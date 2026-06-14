package com.home.tools.dto;

import com.home.tools.entity.MaintenanceType;
import com.home.tools.entity.ToolStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MaintenanceTrackDTO {

    public enum TrackType {
        USAGE, MAINTENANCE, INVENTORY, STATUS_CHANGE
    }

    private TrackType type;
    private Long recordId;
    private LocalDate actionDate;
    private LocalDateTime actionTime;
    private String actionName;
    private String description;
    private Integer durationMinutes;
    private BigDecimal cost;
    private String operator;
    private ToolStatus oldStatus;
    private ToolStatus newStatus;
    private MaintenanceType maintenanceType;
    private Boolean inventoryChecked;
    private String inventoryActualStatus;

    public TrackType getType() { return type; }
    public void setType(TrackType type) { this.type = type; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public LocalDate getActionDate() { return actionDate; }
    public void setActionDate(LocalDate actionDate) { this.actionDate = actionDate; }
    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public ToolStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(ToolStatus oldStatus) { this.oldStatus = oldStatus; }
    public ToolStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ToolStatus newStatus) { this.newStatus = newStatus; }
    public MaintenanceType getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(MaintenanceType maintenanceType) { this.maintenanceType = maintenanceType; }
    public Boolean getInventoryChecked() { return inventoryChecked; }
    public void setInventoryChecked(Boolean inventoryChecked) { this.inventoryChecked = inventoryChecked; }
    public String getInventoryActualStatus() { return inventoryActualStatus; }
    public void setInventoryActualStatus(String inventoryActualStatus) { this.inventoryActualStatus = inventoryActualStatus; }
}
