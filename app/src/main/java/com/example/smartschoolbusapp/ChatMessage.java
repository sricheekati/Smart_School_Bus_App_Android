package com.example.smartschoolbusapp;

public class ChatMessage extends ChatItem {
    private String senderID;
    private String receiverID;
    private String message;
    private long timestamp;

    public ChatMessage() { }
    @Override
    public int getType() {
        return TYPE_MESSAGE;
    }
    public ChatMessage(String senderID, String receiverID, String message, long timestamp) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.message = message;
        this.timestamp = timestamp;
    }
    private boolean seen;

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public String getSenderID() { return senderID; }
    public String getReceiverID() { return receiverID; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}