package com.home.tools.dto;

public class InventoryCompletionResultDTO {

    public InventoryCompletionResultDTO() {}

    private Boolean success;

    private String message;

    private Integer totalUpdated;

    private Integer statusMismatchCount;

    private Integer lostCount;

    private Integer checkedCount;

    private Integer uncheckedCount;

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Integer getTotalUpdated() { return totalUpdated; }
    public void setTotalUpdated(Integer totalUpdated) { this.totalUpdated = totalUpdated; }
    public Integer getStatusMismatchCount() { return statusMismatchCount; }
    public void setStatusMismatchCount(Integer statusMismatchCount) { this.statusMismatchCount = statusMismatchCount; }
    public Integer getLostCount() { return lostCount; }
    public void setLostCount(Integer lostCount) { this.lostCount = lostCount; }
    public Integer getCheckedCount() { return checkedCount; }
    public void setCheckedCount(Integer checkedCount) { this.checkedCount = checkedCount; }
    public Integer getUncheckedCount() { return uncheckedCount; }
    public void setUncheckedCount(Integer uncheckedCount) { this.uncheckedCount = uncheckedCount; }
}
