package com.home.tools.dto;

import java.time.LocalDate;

public class InventoryDTO {

    public InventoryDTO() {}

    private LocalDate inventoryDate;
    private String operator;
    private String remarks;

    public LocalDate getInventoryDate() { return inventoryDate; }
    public void setInventoryDate(LocalDate inventoryDate) { this.inventoryDate = inventoryDate; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
