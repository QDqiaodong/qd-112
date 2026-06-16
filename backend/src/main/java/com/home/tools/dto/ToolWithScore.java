package com.home.tools.dto;

import com.home.tools.entity.Tool;

public class ToolWithScore {

    private Tool tool;
    private ToolAvailabilityScore availabilityScore;

    public ToolWithScore() {}

    public ToolWithScore(Tool tool, ToolAvailabilityScore availabilityScore) {
        this.tool = tool;
        this.availabilityScore = availabilityScore;
    }

    public Tool getTool() { return tool; }
    public void setTool(Tool tool) { this.tool = tool; }

    public ToolAvailabilityScore getAvailabilityScore() { return availabilityScore; }
    public void setAvailabilityScore(ToolAvailabilityScore availabilityScore) { this.availabilityScore = availabilityScore; }
}
