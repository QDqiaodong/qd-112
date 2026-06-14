package com.home.tools.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tool_status_history")
public class ToolStatusHistory {

    public ToolStatusHistory() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long toolId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ToolStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ToolStatus newStatus;

    private String operator;

    private String reason;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public ToolStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(ToolStatus oldStatus) { this.oldStatus = oldStatus; }
    public ToolStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ToolStatus newStatus) { this.newStatus = newStatus; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
