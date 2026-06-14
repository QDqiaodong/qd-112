package com.home.tools.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventories")
public class Inventory {

    public Inventory() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate inventoryDate;

    private Integer totalTools;

    private Integer checkedTools;

    private Integer availableTools;

    private Integer loanedTools;

    private Integer maintenanceTools;

    private Integer lostTools;

    private String operator;

    private String remarks;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getInventoryDate() { return inventoryDate; }
    public void setInventoryDate(LocalDate inventoryDate) { this.inventoryDate = inventoryDate; }
    public Integer getTotalTools() { return totalTools; }
    public void setTotalTools(Integer totalTools) { this.totalTools = totalTools; }
    public Integer getCheckedTools() { return checkedTools; }
    public void setCheckedTools(Integer checkedTools) { this.checkedTools = checkedTools; }
    public Integer getAvailableTools() { return availableTools; }
    public void setAvailableTools(Integer availableTools) { this.availableTools = availableTools; }
    public Integer getLoanedTools() { return loanedTools; }
    public void setLoanedTools(Integer loanedTools) { this.loanedTools = loanedTools; }
    public Integer getMaintenanceTools() { return maintenanceTools; }
    public void setMaintenanceTools(Integer maintenanceTools) { this.maintenanceTools = maintenanceTools; }
    public Integer getLostTools() { return lostTools; }
    public void setLostTools(Integer lostTools) { this.lostTools = lostTools; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
