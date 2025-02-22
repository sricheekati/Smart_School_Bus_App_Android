package com.example.smartschoolbusapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DriverDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private TextView menuChat, menuRoutes, menuEmergencyAlerts, menuLogout;
    private View menuApproveUsers;

    // Search Functionality
    private RecyclerView searchResultsList;
    private UserAdapter userAdapter;
    private List<UserModel> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ✅ Ensure Toolbar Title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Driver Dashboard");
        }

        // ✅ Initialize RecyclerView for search results
        searchResultsList = findViewById(R.id.search_results_list);
        searchResultsList.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, this);
        searchResultsList.setAdapter(userAdapter);
        searchResultsList.setVisibility(View.GONE); // Hide initially

        setupNavigationDrawer();
    }

    // ✅ Setup Sidebar Menu (Removed Approve Users)
    private void setupNavigationDrawer() {
        View headerView = navigationView.getHeaderView(0);
        menuChat = headerView.findViewById(R.id.menuChat);
        menuRoutes = headerView.findViewById(R.id.menuRoutes);
        menuLogout = headerView.findViewById(R.id.menuLogout);
        TextView menuEmergencyAlerts = headerView.findViewById(R.id.menuEmergencyAlerts); // ✅ Add Emergency Alerts option
        TextView menuApproveUsers = headerView.findViewById(R.id.menuApproveUsers); // ✅ Find Approve Users item

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, findViewById(R.id.toolbar), R.string.open, R.string.close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        menuChat.setOnClickListener(v -> startActivity(new Intent(this, ChatsListActivity.class)));
        menuRoutes.setOnClickListener(v -> startActivity(new Intent(this, RoutesActivity.class)));

        menuLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(DriverDashboardActivity.this, LoginActivity.class));
            finish();
        });
        // ✅ Hide "Approve Users" for Parents
        firestore.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role != null && role.equals("driver")) {
                            if (menuApproveUsers != null) {
                                menuApproveUsers.setVisibility(View.GONE); // ✅ Hide Approve Users
                            }
                        }
                    }
                });
        // ✅ Open Emergency Alerts Page
        menuEmergencyAlerts.setOnClickListener(v -> {
            startActivity(new Intent(DriverDashboardActivity.this, EmergencyAlertsActivity.class));
        });
    }

    // ✅ Toolbar Search for Parents & Admins
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Search Parents & Admins");

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

            // ✅ Handle search collapse (hide search results)
            searchView.setOnCloseListener(() -> {
                searchResultsList.setVisibility(View.GONE);
                return false;
            });
        }
        return true;
    }

    // ✅ Search Parents & Admins
    private void searchUsers(String query) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 If query is empty, hide search results and return
        if (query.trim().isEmpty()) {
            searchResultsList.setVisibility(View.GONE);
            return;
        }

        firestore.collection("users")
                .whereIn("role", Arrays.asList("admin", "parent")) // ✅ Search only Admins & Parents
                .whereEqualTo("status", "approved") // ✅ Only show approved users
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user != null && user.getName() != null && user.getName().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(user);
                        }
                    }

                    if (userList.isEmpty()) {
                        Toast.makeText(this, "No users found!", Toast.LENGTH_SHORT).show();
                        searchResultsList.setVisibility(View.GONE); // ✅ Hide list if no results
                    } else {
                        userAdapter.updateList(userList);
                        searchResultsList.setVisibility(View.VISIBLE); // ✅ Show results
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreSearch", "Failed to search users: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to search users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}