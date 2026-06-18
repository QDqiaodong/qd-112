package com.home.tools.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PartReplacementDTO {

    public PartReplacementDTO() {}

    private Long toolId;
    private String partName;
    private String partType;
    private LocalDate replacementDate;
    private BigDecimal cost;
    private String operator;
    private String supplier;
    private String remarks;

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public String getPartType() { return partType; }
    public void setPartType(String partType) { this.partType = partType; }
    public LocalDate getReplacementDate() { return replacementDate; }
    public void setReplacementDate(LocalDate replacementDate) { this.replacementDate = replacementDate; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
