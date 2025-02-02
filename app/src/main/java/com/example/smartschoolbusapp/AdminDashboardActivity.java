package com.example.smartschoolbusapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView pendingList;
    private Button approveAllButton;
    private FirebaseFirestore firestore;
    private PendingUsersAdapter adapter;
    private List<DocumentSnapshot> pendingUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize views
        pendingList = findViewById(R.id.pending_list);
        approveAllButton = findViewById(R.id.btn_approve_all);
        firestore = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        pendingList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendingUsersAdapter(pendingUsers, this);
        pendingList.setAdapter(adapter);

        // Load pending users
        loadPendingUsers();

        // Approve all users
        approveAllButton.setOnClickListener(v -> approveAllUsers());
    }

    private void loadPendingUsers() {
        firestore.collection("users").whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        pendingUsers.clear();
                        pendingUsers.addAll(task.getResult().getDocuments());
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(AdminDashboardActivity.this, "Failed to load users!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void approveUser(String userId) {
        firestore.collection("users").document(userId)
                .update("status", "approved")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminDashboardActivity.this, "User Approved!", Toast.LENGTH_SHORT).show();
                    loadPendingUsers(); // Refresh list after approval
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminDashboardActivity.this, "Approval Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void approveAllUsers() {
        for (DocumentSnapshot user : pendingUsers) {
            approveUser(user.getId());
        }
    }
}