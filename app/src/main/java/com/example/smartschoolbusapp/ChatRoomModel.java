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
    public String getLastMessage() { return lastMessage; }
    public long getTimestamp() { return timestamp; }
    public List<String> getUsers() { return users; }
    public Map<String, String> getRoles() { return roles; }

    // ✅ Add receiverId Getter & Setter
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; } // 🔥 Add this method
}