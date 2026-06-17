package com.home.tools.dto;

public class StatusTransitionResult {

    private boolean valid;
    private String message;
    private String reason;

    public StatusTransitionResult() {}

    public StatusTransitionResult(boolean valid, String message, String reason) {
        this.valid = valid;
        this.message = message;
        this.reason = reason;
    }

    public static StatusTransitionResult success() {
        return new StatusTransitionResult(true, "状态变更合法", null);
    }

    public static StatusTransitionResult fail(String message, String reason) {
        return new StatusTransitionResult(false, message, reason);
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
