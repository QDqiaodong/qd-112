package com.home.tools.entity;

public enum MaintenanceDueStatus {

    NORMAL("正常", 0),
    APPROACHING("即将到期", 1),
    DUE_TODAY("今日到期", 2),
    OVERDUE("已逾期", 3),
    SEVERELY_OVERDUE("严重逾期", 4),
    LONG_TERM_OVERDUE("长期逾期", 5),
    NO_CYCLE("无周期设定", -1);

    private final String description;
    private final int severity;

    MaintenanceDueStatus(String description, int severity) {
        this.description = description;
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public int getSeverity() {
        return severity;
    }

    public boolean isDueOrOverdue() {
        return severity >= 2;
    }

    public boolean isWarning() {
        return severity >= 1 && severity <= 1;
    }
}
