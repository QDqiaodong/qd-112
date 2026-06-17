package com.home.tools.dto;

import com.home.tools.entity.MaintenanceItem;

public class MaintenanceItemWithSource {

    private MaintenanceItem item;
    private String sourceType;
    private String sourceCategoryId;
    private String sourceCategoryName;
    private Integer effectiveCycleDays;
    private String cycleSource;
    private Boolean enabled;
    private Boolean overridden;
    private Integer overriddenCycleDays;
    private String remarks;

    public MaintenanceItem getItem() {
        return item;
    }

    public void setItem(MaintenanceItem item) {
        this.item = item;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceCategoryId() {
        return sourceCategoryId;
    }

    public void setSourceCategoryId(String sourceCategoryId) {
        this.sourceCategoryId = sourceCategoryId;
    }

    public String getSourceCategoryName() {
        return sourceCategoryName;
    }

    public void setSourceCategoryName(String sourceCategoryName) {
        this.sourceCategoryName = sourceCategoryName;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getOverridden() {
        return overridden;
    }

    public void setOverridden(Boolean overridden) {
        this.overridden = overridden;
    }

    public Integer getOverriddenCycleDays() {
        return overriddenCycleDays;
    }

    public void setOverriddenCycleDays(Integer overriddenCycleDays) {
        this.overriddenCycleDays = overriddenCycleDays;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
