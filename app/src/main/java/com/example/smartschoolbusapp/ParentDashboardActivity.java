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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParentDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private TextView menuChat, menuRoutes, menuLogout, busLocation, notificationText;

    // RecyclerView & Adapter for search results
    private RecyclerView searchResultsList;
    private UserAdapter userAdapter;
    private List<UserModel> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        busLocation = findViewById(R.id.bus_location);
        notificationText = findViewById(R.id.notification_text);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ✅ Fix: Check if getSupportActionBar() is not null before calling setTitle()
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Parent Dashboard");
        }

        // ✅ Initialize RecyclerView for search results
        searchResultsList = findViewById(R.id.search_results_list);
        if (searchResultsList != null) {
            searchResultsList.setLayoutManager(new LinearLayoutManager(this));
            userAdapter = new UserAdapter(userList, this);
            searchResultsList.setAdapter(userAdapter);
        } else {
            Log.e("ParentDashboardActivity", "RecyclerView not found in XML!");
        }
        // ✅ Hide search results initially
        searchResultsList.setVisibility(View.GONE);

        // ✅ Sidebar Setup
        setupNavigationDrawer();

        // ✅ Listen for Alerts
//        listenForEmergencyAlerts();
    }

    // ✅ Sidebar Setup
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
            startActivity(new Intent(ParentDashboardActivity.this, LoginActivity.class));
            finish();
        });
        // ✅ Hide "Approve Users" for Parents
        firestore.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role != null && role.equals("parent")) {
                            if (menuApproveUsers != null) {
                                menuApproveUsers.setVisibility(View.GONE); // ✅ Hide Approve Users
                            }
                        }
                    }
                });
        // ✅ Open Emergency Alerts Page
        menuEmergencyAlerts.setOnClickListener(v -> {
            startActivity(new Intent(ParentDashboardActivity.this, EmergencyAlertsActivity.class));
        });
    }
    // ✅ Listen for Emergency Alerts
//    private void listenForEmergencyAlerts() {
//        firestore.collection("emergency_alerts")
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .addSnapshotListener((snapshots, error) -> {
//                    if (error != null) {
//                        Toast.makeText(ParentDashboardActivity.this, "Error loading alerts!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    StringBuilder alertsText = new StringBuilder();
//
//                    for (DocumentChange doc : snapshots.getDocumentChanges()) {
//                        if (doc.getType() == DocumentChange.Type.ADDED) {
//                            String alertMessage = doc.getDocument().getString("message");
//                            String senderRole = doc.getDocument().getString("sender_role");
//
//                            // ✅ Fetch Timestamp Safely
//                            Object timestampObj = doc.getDocument().get("timestamp");
//                            String formattedTime = "Unknown Time"; // Default
//
//                            if (timestampObj instanceof Timestamp) {
//                                Date date = ((Timestamp) timestampObj).toDate();
//                                formattedTime = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(date);
//                            } else if (timestampObj instanceof Long) {
//                                Date date = new Date((Long) timestampObj);
//                                formattedTime = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(date);
//                            }
//
//                            // ✅ Append formatted alert
//                            if (alertMessage != null && senderRole != null) {
//                                alertsText.append("🚨 [").append(senderRole.toUpperCase()).append("] ")
//                                        .append(alertMessage).append("\n")
//                                        .append("🕒 ").append(formattedTime).append("\n\n");
//                            }
//                        }
//                    }
//
//                    // ✅ Update UI
//                    if (alertsText.length() > 0) {
//                        notificationText.setText(alertsText.toString());
//                    } else {
//                        notificationText.setText("No Emergency Alerts");
//                    }
//                });
//    }

    // ✅ Search Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Search Admins & Drivers");

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

    // ✅ Search Admins & Drivers
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
                .whereIn("role", Arrays.asList("admin", "driver")) // ✅ FIXED: Use Arrays.asList()
                .whereEqualTo("status", "approved")  // ✅ Only fetch Approved Users
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();

                    /*for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user != null && user.getName() != null && user.getName().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(user);
                        }
                    }*/

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user != null) {
                            user.setUid(doc.getId()); // ✅ Set UID from document ID
                            if (user.getName() != null && user.getName().toLowerCase().contains(query.toLowerCase())) {
                                userList.add(user);
                            }
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