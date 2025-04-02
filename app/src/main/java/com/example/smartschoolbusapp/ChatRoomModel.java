package com.example.smartschoolbusapp;

import java.util.List;
import java.util.Map;

public class ChatRoomModel {
    private String chatRoomId;
    private String lastMessage;
    private long timestamp;
    private List<String> users;
    private List<String> seenBy;
    private Map<String, Integer> unreadCount;
    private String receiverId;

    public ChatRoomModel() {}

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(List<String> seenBy) {
        this.seenBy = seenBy;
    }

    public Map<String, Integer> getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Map<String, Integer> unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getUnreadCountForUser(String userId) {
        if (unreadCount != null && unreadCount.containsKey(userId)) {
            return unreadCount.get(userId);
        }
        return 0;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
}