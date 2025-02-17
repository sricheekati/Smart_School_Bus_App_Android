package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements PendingUsersAdapter.OnUserApprovedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private RecyclerView pendingList, usersList;
    private PendingUsersAdapter pendingUsersAdapter;
    private UserAdapter userAdapter;
    private List<UserModel> pendingUsers = new ArrayList<>();
    private List<UserModel> allUsers = new ArrayList<>();
    private List<UserModel> filteredUsers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // ✅ Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // ✅ Initialize Views
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        pendingList = findViewById(R.id.pending_list);
        usersList = findViewById(R.id.user_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Admin Dashboard");

        if (pendingList == null || usersList == null) {
            Toast.makeText(this, "RecyclerView not found! Check XML file IDs", Toast.LENGTH_LONG).show();
            return;
        }

        setSupportActionBar(toolbar);

        // ✅ Setup Sidebar Menu
        setupNavigationDrawer();

        // ✅ Setup RecyclerViews
        setupRecyclerViews();
    }

    // ✅ Setup Sidebar Menu & Click Listeners
    private void setupNavigationDrawer() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) {
            Toast.makeText(this, "Sidebar menu error!", Toast.LENGTH_LONG).show();
            return;
        }

        ImageView schoolBusIcon = headerView.findViewById(R.id.schoolBusIcon);
        TextView menuChat = headerView.findViewById(R.id.menuChat);
        TextView menuRoutes = headerView.findViewById(R.id.menuRoutes);
        TextView menuApproveUsers = headerView.findViewById(R.id.menuApproveUsers);
        TextView menuLogout = headerView.findViewById(R.id.menuLogout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, findViewById(R.id.toolbar), R.string.open, R.string.close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        menuChat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        menuRoutes.setOnClickListener(v -> startActivity(new Intent(this, RoutesActivity.class)));
        menuApproveUsers.setOnClickListener(v -> startActivity(new Intent(this, ApproveUsersActivity.class)));
        menuLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        schoolBusIcon.setOnClickListener(v ->
                Toast.makeText(this, "School Bus Icon Clicked!", Toast.LENGTH_SHORT).show()
        );
    }

    // ✅ Setup RecyclerViews
    private void setupRecyclerViews() {
        pendingList.setLayoutManager(new LinearLayoutManager(this));
        pendingUsersAdapter = new PendingUsersAdapter(pendingUsers, this);
        pendingList.setAdapter(pendingUsersAdapter);

        usersList.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(allUsers, this);
        usersList.setAdapter(userAdapter);
    }

    // ✅ Approve User (Implements Interface)
    @Override
    public void onUserApproved(String userId) {
        firestore.collection("users").document(userId)
                .update("status", "approved")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User Approved!", Toast.LENGTH_SHORT).show();
//                    loadPendingUsers();
//                    loadApprovedUsers();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Approval Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onUserRejected(String userId) {
        firestore.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User Rejected!", Toast.LENGTH_SHORT).show();
//                    loadApprovedUsers();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Rejection Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ Load Approved Users (Users with "approved" status)
    private void loadApprovedUsers() {
        firestore.collection("users").whereEqualTo("status", "approved")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        allUsers.clear();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            UserModel user = document.toObject(UserModel.class);
                            if (user != null) {
                                user.setUid(document.getId());
                                allUsers.add(user);
                            }
                        }
                        userAdapter.updateList(allUsers);  // ✅ Ensure UI refreshes with approved users
                    }
                });
    }

    // Toolbar Search Implementation
    // ✅ Toolbar Search Implementation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
            searchView.setQueryHint("Search Users");

            searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchUsers(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    searchUsers(newText);
                    return true;
                }
            });
        }
        return true;
    }

    // ✅ Search function for dynamic user filtering
    private void searchUsers(String query) {
        List<UserModel> filteredList = new ArrayList<>();
        for (UserModel user : allUsers) {
            if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        userAdapter.updateList(filteredList); // ✅ Dynamically update UI with search results
    }
}