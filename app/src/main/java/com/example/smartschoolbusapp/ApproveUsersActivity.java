package com.example.smartschoolbusapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ApproveUsersActivity extends AppCompatActivity implements PendingUsersAdapter.OnUserApprovedListener {

    private RecyclerView pendingList;
    private PendingUsersAdapter pendingUsersAdapter;
    private List<UserModel> pendingUsers = new ArrayList<>();
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_users);

        // ✅ Setup Toolbar with Back Button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Approve Users");

        // ✅ Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // ✅ Setup RecyclerView
        pendingList = findViewById(R.id.pending_list);
        pendingList.setLayoutManager(new LinearLayoutManager(this));
        pendingUsersAdapter = new PendingUsersAdapter(pendingUsers, this);
        pendingList.setAdapter(pendingUsersAdapter);

        // ✅ Load Pending Users
        loadPendingUsers();
    }

    // ✅ Load Only Pending Users
    private void loadPendingUsers() {
        firestore.collection("users").whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        pendingUsers.clear();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            UserModel user = document.toObject(UserModel.class);
                            if (user != null) {
                                user.setUid(document.getId()); // Assign Firestore ID
                                pendingUsers.add(user);
                            }
                        }
                        pendingUsersAdapter.notifyDataSetChanged();

                        // ✅ If No Pending Users Left, Show Message
                        if (pendingUsers.isEmpty()) {
                            Toast.makeText(this, "No pending users left!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error loading pending users!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ✅ Approve User (Auto-Refresh List)
    @Override
    public void onUserApproved(String userId) {
        firestore.collection("users").document(userId)
                .update("status", "approved")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User Approved!", Toast.LENGTH_SHORT).show();
                    removeUserFromPendingList(userId); // ✅ Remove User After Approving
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Approval Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ Reject User (Auto-Refresh List)
    @Override
    public void onUserRejected(String userId) {
        firestore.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User Rejected & Removed!", Toast.LENGTH_SHORT).show();
                    removeUserFromPendingList(userId); // ✅ Remove User After Rejecting
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Rejection Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ Remove User from the Pending List
    private void removeUserFromPendingList(String userId) {
        for (int i = 0; i < pendingUsers.size(); i++) {
            if (pendingUsers.get(i).getUid().equals(userId)) {
                pendingUsers.remove(i);
                break;
            }
        }
        pendingUsersAdapter.notifyDataSetChanged();

        // ✅ If No Pending Users Left, Show Message
        if (pendingUsers.isEmpty()) {
            Toast.makeText(this, "No pending users left!", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Handle Back Button Click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}