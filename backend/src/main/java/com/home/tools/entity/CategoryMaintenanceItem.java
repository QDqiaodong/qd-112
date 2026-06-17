package com.home.tools.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_maintenance_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"categoryId", "maintenanceItemCode"})
})
public class CategoryMaintenanceItem {

    public CategoryMaintenanceItem() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private String maintenanceItemCode;

    private Integer customCycleDays;

    private Boolean enabled;

    private String remarks;

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (enabled == null) {
            enabled = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getMaintenanceItemCode() {
        return maintenanceItemCode;
    }

    public void setMaintenanceItemCode(String maintenanceItemCode) {
        this.maintenanceItemCode = maintenanceItemCode;
    }

    public Integer getCustomCycleDays() {
        return customCycleDays;
    }

    public void setCustomCycleDays(Integer customCycleDays) {
        this.customCycleDays = customCycleDays;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
