package com.example.smartschoolbusapp;

public class EmergencyAlertModel {
    private String message;
    private String senderName;
    private String senderRole;
    private long timestamp;

    public EmergencyAlertModel() {}

    public EmergencyAlertModel(String message, String senderName, String senderRole, long timestamp) {
        this.message = message;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public long getTimestamp() {
        return timestamp;
    }
}