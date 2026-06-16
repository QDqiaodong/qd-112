package com.home.tools.dto;

public class CategoryDeletionCheck {

    private boolean canDelete;
    private long toolCount;
    private long subCategoryCount;
    private long maintenanceRecordCount;
    private String message;

    public CategoryDeletionCheck() {}

    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }
    public long getToolCount() { return toolCount; }
    public void setToolCount(long toolCount) { this.toolCount = toolCount; }
    public long getSubCategoryCount() { return subCategoryCount; }
    public void setSubCategoryCount(long subCategoryCount) { this.subCategoryCount = subCategoryCount; }
    public long getMaintenanceRecordCount() { return maintenanceRecordCount; }
    public void setMaintenanceRecordCount(long maintenanceRecordCount) { this.maintenanceRecordCount = maintenanceRecordCount; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
