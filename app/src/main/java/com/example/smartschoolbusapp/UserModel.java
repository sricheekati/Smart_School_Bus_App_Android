package com.example.smartschoolbusapp;

public class UserModel {
    private String uid;
    private String name;
    private String email;
    private String role;  // New Field
    private String status; // "approved" or "pending"

    // ✅ Empty Constructor for Firestore
    public UserModel() {}

    // ✅ Constructor
    public UserModel(String uid, String name, String email, String role, String status) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    // ✅ Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}