package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private RecyclerView pendingList;
    private PendingUsersAdapter adapter;
    private TextView menuChat, menuRoutes, menuApproveUsers, menuLogout;
    private List<DocumentSnapshot> pendingUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        pendingList = findViewById(R.id.pending_list);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        View headerView = navigationView.getHeaderView(0);
        menuChat = headerView.findViewById(R.id.menuChat);
        menuRoutes = headerView.findViewById(R.id.menuRoutes);
        menuApproveUsers = headerView.findViewById(R.id.menuApproveUsers);
        menuLogout = headerView.findViewById(R.id.menuLogout);

        // ✅ Sidebar Toggle Fix
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        menuApproveUsers.setVisibility(View.VISIBLE);

        menuChat.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, ChatActivity.class)));
        menuRoutes.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, RoutesActivity.class)));
        menuApproveUsers.setOnClickListener(v -> loadPendingUsers());

        menuLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
            finish();
        });

        // ✅ Set up Pending Users List
        pendingList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendingUsersAdapter(pendingUsers, this);
        pendingList.setAdapter(adapter);

        loadPendingUsers(); // Load users when the activity starts
    }

    // ✅ Load Pending Users from Firestore
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

    // ✅ Approve User
    public void approveUser(String userId) {
        firestore.collection("users").document(userId)
                .update("status", "approved")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminDashboardActivity.this, "User Approved!", Toast.LENGTH_SHORT).show();
                    loadPendingUsers(); // Refresh pending users list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminDashboardActivity.this, "Approval Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}