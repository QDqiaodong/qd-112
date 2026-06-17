package com.home.tools.dto;

import java.util.Map;

public class MaintenanceDashboardStats {

    private long totalTools;
    private long normalCount;
    private long approachingCount;
    private long dueTodayCount;
    private long overdueCount;
    private long severelyOverdueCount;
    private long longTermOverdueCount;
    private long noCycleCount;
    private long dueOrOverdueTotal;
    private Map<String, Long> byCategory;
    private Double overdueRate;

    public long getTotalTools() {
        return totalTools;
    }

    public void setTotalTools(long totalTools) {
        this.totalTools = totalTools;
    }

    public long getNormalCount() {
        return normalCount;
    }

    public void setNormalCount(long normalCount) {
        this.normalCount = normalCount;
    }

    public long getApproachingCount() {
        return approachingCount;
    }

    public void setApproachingCount(long approachingCount) {
        this.approachingCount = approachingCount;
    }

    public long getDueTodayCount() {
        return dueTodayCount;
    }

    public void setDueTodayCount(long dueTodayCount) {
        this.dueTodayCount = dueTodayCount;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public long getSeverelyOverdueCount() {
        return severelyOverdueCount;
    }

    public void setSeverelyOverdueCount(long severelyOverdueCount) {
        this.severelyOverdueCount = severelyOverdueCount;
    }

    public long getLongTermOverdueCount() {
        return longTermOverdueCount;
    }

    public void setLongTermOverdueCount(long longTermOverdueCount) {
        this.longTermOverdueCount = longTermOverdueCount;
    }

    public long getNoCycleCount() {
        return noCycleCount;
    }

    public void setNoCycleCount(long noCycleCount) {
        this.noCycleCount = noCycleCount;
    }

    public long getDueOrOverdueTotal() {
        return dueOrOverdueTotal;
    }

    public void setDueOrOverdueTotal(long dueOrOverdueTotal) {
        this.dueOrOverdueTotal = dueOrOverdueTotal;
    }

    public Map<String, Long> getByCategory() {
        return byCategory;
    }

    public void setByCategory(Map<String, Long> byCategory) {
        this.byCategory = byCategory;
    }

    public Double getOverdueRate() {
        return overdueRate;
    }

    public void setOverdueRate(Double overdueRate) {
        this.overdueRate = overdueRate;
    }
}
