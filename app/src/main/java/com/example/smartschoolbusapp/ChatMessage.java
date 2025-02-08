package com.example.smartschoolbusapp;

public class ChatMessage {
    private String senderID;
    private String receiverID;
    private String message;
    private long timestamp;

    public ChatMessage() { }

    public ChatMessage(String senderID, String receiverID, String message, long timestamp) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderID() { return senderID; }
    public String getReceiverID() { return receiverID; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}