package com.example.smartschoolbusapp;

import java.util.List;
import java.util.Map;

public class ChatRoomModel {
    private String lastMessage;
    private long timestamp;
    private List<String> users;
    private Map<String, String> roles;
    private String receiverId; // ✅ Add receiverId field

    // ✅ Default Constructor (Needed for Firestore)
    public ChatRoomModel() { }

    // ✅ Constructor
    public ChatRoomModel(String lastMessage, long timestamp, List<String> users, Map<String, String> roles) {
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.users = users;
        this.roles = roles;
    }

    // ✅ Getters
    private List<String> seenBy;

    public List<String> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(List<String> seenBy) {
        this.seenBy = seenBy;
    }
    private int unreadCount;

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLastMessage() { return lastMessage; }
    public long getTimestamp() { return timestamp; }
    public List<String> getUsers() { return users; }
    public Map<String, String> getRoles() { return roles; }

    // ✅ Add receiverId Getter & Setter
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; } // 🔥 Add this method
}